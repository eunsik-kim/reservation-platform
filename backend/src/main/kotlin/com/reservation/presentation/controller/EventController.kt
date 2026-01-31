package com.reservation.presentation.controller

import com.reservation.application.dto.*
import com.reservation.application.usecase.EventUseCase
import com.reservation.application.usecase.ReservationUseCase
import com.reservation.infrastructure.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/events")
class EventController(
    private val eventUseCase: EventUseCase,
    private val reservationUseCase: ReservationUseCase
) {

    @PostMapping
    fun createEvent(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateEventRequest
    ): ResponseEntity<ApiResponse<EventResponse>> {
        val event = eventUseCase.createEvent(userDetails.id, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(event))
    }

    @GetMapping
    fun getEvents(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<EventListResponse>> {
        val events = eventUseCase.getPublicEvents(page, size)
        return ResponseEntity.ok(ApiResponse.success(events))
    }

    @GetMapping("/my")
    fun getMyEvents(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<EventListResponse>> {
        val events = eventUseCase.getMyEvents(userDetails.id, page, size)
        return ResponseEntity.ok(ApiResponse.success(events))
    }

    @GetMapping("/{eventId}")
    fun getEvent(@PathVariable eventId: Long): ResponseEntity<ApiResponse<EventResponse>> {
        val event = eventUseCase.getEvent(eventId)
        return ResponseEntity.ok(ApiResponse.success(event))
    }

    @PutMapping("/{eventId}")
    fun updateEvent(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable eventId: Long,
        @Valid @RequestBody request: UpdateEventRequest
    ): ResponseEntity<ApiResponse<EventResponse>> {
        val event = eventUseCase.updateEvent(eventId, userDetails.id, request)
        return ResponseEntity.ok(ApiResponse.success(event))
    }

    @DeleteMapping("/{eventId}")
    fun deleteEvent(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable eventId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        eventUseCase.deleteEvent(eventId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @PostMapping("/{eventId}/publish")
    fun publishEvent(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable eventId: Long
    ): ResponseEntity<ApiResponse<EventResponse>> {
        val event = eventUseCase.publishEvent(eventId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(event))
    }

    @GetMapping("/{eventId}/slots")
    fun getSlots(@PathVariable eventId: Long): ResponseEntity<ApiResponse<List<SlotResponse>>> {
        val slots = eventUseCase.getSlots(eventId)
        return ResponseEntity.ok(ApiResponse.success(slots))
    }

    @PostMapping("/{eventId}/slots")
    fun addSlot(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable eventId: Long,
        @Valid @RequestBody request: CreateSlotRequest
    ): ResponseEntity<ApiResponse<SlotResponse>> {
        val slot = eventUseCase.addSlot(eventId, userDetails.id, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(slot))
    }

    @GetMapping("/{eventId}/participants")
    fun getParticipants(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable eventId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<ParticipantListResponse>> {
        val participants = reservationUseCase.getParticipants(eventId, userDetails.id, page, size)
        return ResponseEntity.ok(ApiResponse.success(participants))
    }
}
