package com.reservation.application.usecase

import com.reservation.application.dto.*
import com.reservation.domain.entity.Reservation
import com.reservation.domain.entity.ReservationStatus
import com.reservation.domain.exception.*
import com.reservation.domain.repository.EventRepository
import com.reservation.domain.repository.ReservationRepository
import com.reservation.domain.repository.SlotRepository
import com.reservation.domain.repository.UserRepository
import com.reservation.infrastructure.rabbitmq.ReservationMessageProducer
import com.reservation.infrastructure.rabbitmq.message.CreateReservationMessage
import com.reservation.infrastructure.rabbitmq.message.NotificationMessage
import com.reservation.infrastructure.rabbitmq.message.NotificationType
import com.reservation.infrastructure.redis.RedisLockAdapter
import com.reservation.infrastructure.redis.RedisQueueAdapter
import com.reservation.infrastructure.redis.RedisStockAdapter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationUseCase(
    private val reservationRepository: ReservationRepository,
    private val eventRepository: EventRepository,
    private val slotRepository: SlotRepository,
    private val userRepository: UserRepository,
    private val redisQueueAdapter: RedisQueueAdapter,
    private val redisStockAdapter: RedisStockAdapter,
    private val redisLockAdapter: RedisLockAdapter,
    private val messageProducer: ReservationMessageProducer
) {

    /**
     * 예약 요청 (비동기 처리를 위해 메시지 큐에 전송)
     */
    fun createReservation(userId: Long, request: CreateReservationRequest): ReservationResponse {
        val event = eventRepository.findById(request.eventId)
            .orElseThrow { EventNotFoundException() }

        if (!event.canReserve()) {
            throw EventNotOpenException()
        }

        // 이미 예약이 있는지 확인
        val activeStatuses = listOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED)
        if (reservationRepository.existsByEventIdAndUserIdAndStatusIn(request.eventId, userId, activeStatuses)) {
            throw ReservationAlreadyExistsException()
        }

        // 대기열 사용 시 ready 상태 확인
        val settingsJson = event.settings
        val useQueue = settingsJson.contains("\"useQueue\":true")
        if (useQueue && !redisQueueAdapter.isReady(request.eventId, userId)) {
            throw QueueNotReadyException()
        }

        // 동기적으로 예약 처리 (락 사용)
        return redisLockAdapter.executeWithUserReservationLock(request.eventId, userId) {
            processReservationInternal(request.eventId, userId, request.slotId)
        }
    }

    /**
     * 실제 예약 처리 (Consumer에서 호출 또는 동기 처리)
     */
    @Transactional
    fun processReservation(eventId: Long, userId: Long, slotId: Long?): ReservationResponse {
        return processReservationInternal(eventId, userId, slotId)
    }

    private fun processReservationInternal(eventId: Long, userId: Long, slotId: Long?): ReservationResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        // 재고 확인 및 감소
        val slot = if (slotId != null) {
            val s = slotRepository.findByIdWithLock(slotId)
                .orElseThrow { SlotNotFoundException() }

            if (redisStockAdapter.decrementSlotStock(slotId) < 0) {
                throw SlotSoldOutException()
            }
            s
        } else {
            if (redisStockAdapter.decrementEventStock(eventId) < 0) {
                throw SlotSoldOutException("예약 가능 인원이 초과되었습니다")
            }
            null
        }

        // 예약 생성
        val reservation = Reservation(
            event = event,
            userId = userId,
            slot = slot,
            status = ReservationStatus.CONFIRMED
        )
        reservation.confirm()

        val savedReservation = reservationRepository.save(reservation)

        // 대기열에서 제거 및 상태 업데이트
        redisQueueAdapter.removeFromReadySet(eventId, userId)
        redisQueueAdapter.removeUserQueueStatus(eventId, userId)

        // 알림 전송
        messageProducer.sendNotification(
            NotificationMessage(
                userId = userId,
                type = NotificationType.RESERVATION_CONFIRMED,
                title = "예약 완료",
                message = "${event.title} 예약이 완료되었습니다.",
                data = mapOf("reservationId" to savedReservation.id)
            )
        )

        return ReservationResponse.from(savedReservation)
    }

    @Transactional
    fun cancelReservation(reservationId: Long, userId: Long) {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ReservationNotFoundException() }

        if (reservation.userId != userId) {
            throw ForbiddenException("예약 취소 권한이 없습니다")
        }

        if (reservation.status == ReservationStatus.CANCELLED) {
            return
        }

        reservation.cancel()
        reservationRepository.save(reservation)

        // 재고 복구
        if (reservation.slot != null) {
            redisStockAdapter.incrementSlotStock(reservation.slot.id)
        } else {
            redisStockAdapter.incrementEventStock(reservation.event.id)
        }

        // 알림 전송
        messageProducer.sendNotification(
            NotificationMessage(
                userId = userId,
                type = NotificationType.RESERVATION_CANCELLED,
                title = "예약 취소",
                message = "${reservation.event.title} 예약이 취소되었습니다.",
                data = mapOf("reservationId" to reservationId)
            )
        )
    }

    @Transactional(readOnly = true)
    fun getReservation(reservationId: Long, userId: Long): ReservationResponse {
        val reservation = reservationRepository.findById(reservationId)
            .orElseThrow { ReservationNotFoundException() }

        if (reservation.userId != userId) {
            throw ForbiddenException("예약 조회 권한이 없습니다")
        }

        return ReservationResponse.from(reservation)
    }

    @Transactional(readOnly = true)
    fun getMyReservations(userId: Long, page: Int, size: Int): ReservationListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val reservationPage = reservationRepository.findByUserId(userId, pageable)

        return ReservationListResponse(
            reservations = reservationPage.content.map { ReservationResponse.from(it) },
            totalElements = reservationPage.totalElements,
            totalPages = reservationPage.totalPages,
            currentPage = page
        )
    }

    @Transactional(readOnly = true)
    fun getParticipants(eventId: Long, userId: Long, page: Int, size: Int): ParticipantListResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        if (event.creatorId != userId) {
            throw ForbiddenException("참가자 조회 권한이 없습니다")
        }

        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"))
        val reservationPage = reservationRepository.findByEventId(eventId, pageable)

        val participants = reservationPage.content.mapNotNull { reservation ->
            val user = userRepository.findById(reservation.userId).orElse(null) ?: return@mapNotNull null
            ParticipantResponse(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                reservationId = reservation.id,
                slotName = reservation.slot?.name,
                status = reservation.status,
                reservedAt = reservation.createdAt
            )
        }

        return ParticipantListResponse(
            participants = participants,
            totalElements = reservationPage.totalElements,
            totalPages = reservationPage.totalPages,
            currentPage = page
        )
    }
}
