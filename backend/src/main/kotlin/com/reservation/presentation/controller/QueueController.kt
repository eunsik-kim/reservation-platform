package com.reservation.presentation.controller

import com.reservation.application.dto.ApiResponse
import com.reservation.application.dto.EnterQueueRequest
import com.reservation.application.dto.QueuePositionResponse
import com.reservation.application.usecase.QueueUseCase
import com.reservation.infrastructure.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/queue")
class QueueController(
    private val queueUseCase: QueueUseCase
) {

    @PostMapping("/enter")
    fun enterQueue(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: EnterQueueRequest
    ): ResponseEntity<ApiResponse<QueuePositionResponse>> {
        val position = queueUseCase.enterQueue(request.eventId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(position))
    }

    @GetMapping("/position")
    fun getPosition(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam eventId: Long
    ): ResponseEntity<ApiResponse<QueuePositionResponse>> {
        val position = queueUseCase.getPosition(eventId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(position))
    }

    @DeleteMapping("/leave")
    fun leaveQueue(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam eventId: Long
    ): ResponseEntity<ApiResponse<Boolean>> {
        val result = queueUseCase.leaveQueue(eventId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
