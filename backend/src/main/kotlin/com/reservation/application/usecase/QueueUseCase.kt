package com.reservation.application.usecase

import com.reservation.application.dto.QueuePositionResponse
import com.reservation.application.dto.QueueStatus
import com.reservation.domain.exception.EventNotFoundException
import com.reservation.domain.exception.EventNotOpenException
import com.reservation.domain.repository.EventRepository
import com.reservation.infrastructure.redis.RedisQueueAdapter
import org.springframework.stereotype.Service

@Service
class QueueUseCase(
    private val eventRepository: EventRepository,
    private val redisQueueAdapter: RedisQueueAdapter
) {

    fun enterQueue(eventId: Long, userId: Long): QueuePositionResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        if (!event.canReserve()) {
            throw EventNotOpenException()
        }

        // 이미 대기열에 있는 경우
        if (redisQueueAdapter.isInQueue(eventId, userId)) {
            return getPosition(eventId, userId)
        }

        // 이미 예약 가능 상태인 경우
        if (redisQueueAdapter.isReady(eventId, userId)) {
            return QueuePositionResponse(
                eventId = eventId,
                userId = userId,
                position = 0,
                totalInQueue = redisQueueAdapter.getQueueSize(eventId),
                status = QueueStatus.READY
            )
        }

        // 대기열 진입
        val position = redisQueueAdapter.enterQueue(eventId, userId)
        redisQueueAdapter.setUserQueueStatus(eventId, userId, "WAITING")

        return QueuePositionResponse(
            eventId = eventId,
            userId = userId,
            position = position,
            totalInQueue = redisQueueAdapter.getQueueSize(eventId),
            status = QueueStatus.WAITING,
            estimatedWaitTime = calculateEstimatedWaitTime(position)
        )
    }

    fun leaveQueue(eventId: Long, userId: Long): Boolean {
        redisQueueAdapter.removeUserQueueStatus(eventId, userId)
        return redisQueueAdapter.leaveQueue(eventId, userId)
    }

    fun getPosition(eventId: Long, userId: Long): QueuePositionResponse {
        // 예약 가능 상태 확인
        if (redisQueueAdapter.isReady(eventId, userId)) {
            return QueuePositionResponse(
                eventId = eventId,
                userId = userId,
                position = 0,
                totalInQueue = redisQueueAdapter.getQueueSize(eventId),
                status = QueueStatus.READY
            )
        }

        // 대기열 순위 조회
        val position = redisQueueAdapter.getPosition(eventId, userId)

        return if (position != null) {
            QueuePositionResponse(
                eventId = eventId,
                userId = userId,
                position = position,
                totalInQueue = redisQueueAdapter.getQueueSize(eventId),
                status = QueueStatus.WAITING,
                estimatedWaitTime = calculateEstimatedWaitTime(position)
            )
        } else {
            QueuePositionResponse(
                eventId = eventId,
                userId = userId,
                position = -1,
                totalInQueue = redisQueueAdapter.getQueueSize(eventId),
                status = QueueStatus.NOT_IN_QUEUE
            )
        }
    }

    fun isUserReady(eventId: Long, userId: Long): Boolean {
        return redisQueueAdapter.isReady(eventId, userId)
    }

    private fun calculateEstimatedWaitTime(position: Long): Long {
        // 100명 단위로 5초마다 처리한다고 가정
        val batchSize = 100
        val batchInterval = 5L // seconds
        return (position / batchSize) * batchInterval
    }
}
