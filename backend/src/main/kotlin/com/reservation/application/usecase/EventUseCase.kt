package com.reservation.application.usecase

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.reservation.application.dto.*
import com.reservation.domain.entity.Event
import com.reservation.domain.entity.EventStatus
import com.reservation.domain.entity.Slot
import com.reservation.domain.exception.EventNotFoundException
import com.reservation.domain.exception.ForbiddenException
import com.reservation.domain.repository.EventRepository
import com.reservation.domain.repository.ReservationRepository
import com.reservation.domain.repository.SlotRepository
import com.reservation.infrastructure.redis.RedisStockAdapter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class EventUseCase(
    private val eventRepository: EventRepository,
    private val slotRepository: SlotRepository,
    private val reservationRepository: ReservationRepository,
    private val redisStockAdapter: RedisStockAdapter
) {
    private val objectMapper = jacksonObjectMapper()

    @Transactional
    fun createEvent(creatorId: Long, request: CreateEventRequest): EventResponse {
        val settingsJson = objectMapper.writeValueAsString(request.settings)

        val event = Event(
            creatorId = creatorId,
            title = request.title,
            description = request.description,
            openAt = request.openAt,
            closeAt = request.closeAt,
            maxParticipants = request.maxParticipants,
            status = EventStatus.DRAFT,
            settings = settingsJson
        )

        val savedEvent = eventRepository.save(event)

        // 슬롯 생성
        request.slots?.forEach { slotRequest ->
            val slot = Slot(
                event = savedEvent,
                name = slotRequest.name,
                quantity = slotRequest.quantity,
                price = slotRequest.price
            )
            slotRepository.save(slot)
            savedEvent.slots.add(slot)
        }

        // Redis 재고 초기화
        if (savedEvent.slots.isEmpty()) {
            redisStockAdapter.initializeEventStock(savedEvent.id, request.maxParticipants)
        } else {
            savedEvent.slots.forEach { slot ->
                redisStockAdapter.initializeSlotStock(slot.id, slot.quantity)
            }
        }

        return EventResponse.from(savedEvent, 0)
    }

    @Transactional
    fun updateEvent(eventId: Long, userId: Long, request: UpdateEventRequest): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        if (event.creatorId != userId) {
            throw ForbiddenException("이벤트 수정 권한이 없습니다")
        }

        request.title?.let { event.title = it }
        request.description?.let { event.description = it }
        request.openAt?.let { event.openAt = it }
        request.closeAt?.let { event.closeAt = it }
        request.maxParticipants?.let { event.maxParticipants = it }
        request.status?.let { event.status = it }
        request.settings?.let {
            event.settings = objectMapper.writeValueAsString(it)
        }

        val savedEvent = eventRepository.save(event)
        val currentParticipants = reservationRepository.countActiveReservationsByEventId(eventId).toInt()

        return EventResponse.from(savedEvent, currentParticipants)
    }

    @Transactional
    fun deleteEvent(eventId: Long, userId: Long) {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        if (event.creatorId != userId) {
            throw ForbiddenException("이벤트 삭제 권한이 없습니다")
        }

        eventRepository.delete(event)
    }

    @Transactional(readOnly = true)
    fun getEvent(eventId: Long): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        val currentParticipants = reservationRepository.countActiveReservationsByEventId(eventId).toInt()
        return EventResponse.from(event, currentParticipants)
    }

    @Transactional(readOnly = true)
    fun getPublicEvents(page: Int, size: Int): EventListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "openAt"))
        val statuses = listOf(EventStatus.SCHEDULED, EventStatus.OPEN)
        val eventPage = eventRepository.findByStatusIn(statuses, pageable)

        val events = eventPage.content.map { event ->
            val currentParticipants = reservationRepository.countActiveReservationsByEventId(event.id).toInt()
            EventResponse.from(event, currentParticipants)
        }

        return EventListResponse(
            events = events,
            totalElements = eventPage.totalElements,
            totalPages = eventPage.totalPages,
            currentPage = page
        )
    }

    @Transactional(readOnly = true)
    fun getMyEvents(userId: Long, page: Int, size: Int): EventListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val eventPage = eventRepository.findByCreatorId(userId, pageable)

        val events = eventPage.content.map { event ->
            val currentParticipants = reservationRepository.countActiveReservationsByEventId(event.id).toInt()
            EventResponse.from(event, currentParticipants)
        }

        return EventListResponse(
            events = events,
            totalElements = eventPage.totalElements,
            totalPages = eventPage.totalPages,
            currentPage = page
        )
    }

    @Transactional(readOnly = true)
    fun getSlots(eventId: Long): List<SlotResponse> {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        return event.slots.map { SlotResponse.from(it) }
    }

    @Transactional
    fun addSlot(eventId: Long, userId: Long, request: CreateSlotRequest): SlotResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        if (event.creatorId != userId) {
            throw ForbiddenException("슬롯 추가 권한이 없습니다")
        }

        val slot = Slot(
            event = event,
            name = request.name,
            quantity = request.quantity,
            price = request.price
        )

        val savedSlot = slotRepository.save(slot)
        redisStockAdapter.initializeSlotStock(savedSlot.id, savedSlot.quantity)

        return SlotResponse.from(savedSlot)
    }

    @Transactional
    fun publishEvent(eventId: Long, userId: Long): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { EventNotFoundException() }

        if (event.creatorId != userId) {
            throw ForbiddenException("이벤트 게시 권한이 없습니다")
        }

        event.status = EventStatus.SCHEDULED
        val savedEvent = eventRepository.save(event)

        return EventResponse.from(savedEvent, 0)
    }
}
