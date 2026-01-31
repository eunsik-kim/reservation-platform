package com.reservation.application.scheduler

import com.reservation.application.dto.QueueStatus
import com.reservation.application.dto.QueueUpdateMessage
import com.reservation.domain.entity.EventStatus
import com.reservation.domain.repository.EventRepository
import com.reservation.infrastructure.rabbitmq.ReservationMessageProducer
import com.reservation.infrastructure.rabbitmq.message.NotificationMessage
import com.reservation.infrastructure.rabbitmq.message.NotificationType
import com.reservation.infrastructure.redis.RedisQueueAdapter
import com.reservation.presentation.websocket.WebSocketNotificationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class QueueScheduler(
    private val eventRepository: EventRepository,
    private val redisQueueAdapter: RedisQueueAdapter,
    private val webSocketNotificationService: WebSocketNotificationService,
    private val messageProducer: ReservationMessageProducer,
    @Value("\${queue.batch-size:100}") private val batchSize: Int,
    @Value("\${queue.check-interval:5000}") private val checkInterval: Long
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 대기열에서 사용자를 예약 가능 상태로 전환
     */
    @Scheduled(fixedRateString = "\${queue.check-interval:5000}")
    fun processQueue() {
        val openEvents = eventRepository.findByStatusIn(
            listOf(EventStatus.OPEN),
            org.springframework.data.domain.PageRequest.of(0, 100)
        ).content

        openEvents.forEach { event ->
            try {
                processEventQueue(event.id)
            } catch (e: Exception) {
                logger.error("Failed to process queue for event ${event.id}", e)
            }
        }
    }

    private fun processEventQueue(eventId: Long) {
        // 대기열에서 상위 N명 추출
        val userIds = redisQueueAdapter.popTopUsers(eventId, batchSize.toLong())

        userIds.forEach { userId ->
            // 예약 가능 상태로 전환 (10분 제한)
            redisQueueAdapter.addToReadySet(eventId, userId, 600)
            redisQueueAdapter.setUserQueueStatus(eventId, userId, "READY")

            // WebSocket 알림
            webSocketNotificationService.sendQueueReady(eventId, userId)

            // 푸시 알림
            messageProducer.sendNotification(
                NotificationMessage(
                    userId = userId,
                    type = NotificationType.QUEUE_READY,
                    title = "예약 차례 도착",
                    message = "예약 차례가 되었습니다. 10분 내에 예약을 완료해주세요.",
                    data = mapOf("eventId" to eventId)
                )
            )

            logger.info("User $userId is now ready to reserve for event $eventId")
        }

        // 남은 대기자들에게 순위 업데이트 전송
        broadcastQueuePositions(eventId)
    }

    private fun broadcastQueuePositions(eventId: Long) {
        val topUsers = redisQueueAdapter.peekTopUsers(eventId, 1000)
        val totalInQueue = redisQueueAdapter.getQueueSize(eventId)

        topUsers.forEachIndexed { index, userId ->
            val message = QueueUpdateMessage(
                eventId = eventId,
                userId = userId,
                position = (index + 1).toLong(),
                totalInQueue = totalInQueue,
                status = QueueStatus.WAITING
            )
            webSocketNotificationService.sendQueueUpdate(eventId, userId, message)
        }
    }

    /**
     * 이벤트 상태 자동 전환 (SCHEDULED -> OPEN, OPEN -> CLOSED)
     */
    @Scheduled(fixedRate = 60000) // 1분마다
    @Transactional
    fun updateEventStatuses() {
        val now = LocalDateTime.now()

        // SCHEDULED -> OPEN
        val eventsToOpen = eventRepository.findEventsToOpen(now)
        eventsToOpen.forEach { event ->
            event.status = EventStatus.OPEN
            eventRepository.save(event)
            logger.info("Event ${event.id} is now OPEN")
        }

        // OPEN -> CLOSED
        val eventsToClose = eventRepository.findEventsToClose(now)
        eventsToClose.forEach { event ->
            event.status = EventStatus.CLOSED
            eventRepository.save(event)
            redisQueueAdapter.clearQueue(event.id)
            logger.info("Event ${event.id} is now CLOSED")
        }
    }
}
