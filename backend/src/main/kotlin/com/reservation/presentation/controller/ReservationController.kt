package com.reservation.presentation.controller

import com.reservation.application.dto.*
import com.reservation.application.usecase.ReservationUseCase
import com.reservation.infrastructure.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val reservationUseCase: ReservationUseCase
) {

    @PostMapping
    fun createReservation(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreateReservationRequest
    ): ResponseEntity<ApiResponse<ReservationResponse>> {
        val reservation = reservationUseCase.createReservation(userDetails.id, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(reservation))
    }

    @GetMapping("/{reservationId}")
    fun getReservation(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable reservationId: Long
    ): ResponseEntity<ApiResponse<ReservationResponse>> {
        val reservation = reservationUseCase.getReservation(reservationId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(reservation))
    }

    @GetMapping("/my")
    fun getMyReservations(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<ReservationListResponse>> {
        val reservations = reservationUseCase.getMyReservations(userDetails.id, page, size)
        return ResponseEntity.ok(ApiResponse.success(reservations))
    }

    @DeleteMapping("/{reservationId}")
    fun cancelReservation(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable reservationId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        reservationUseCase.cancelReservation(reservationId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}
