package com.reservation.presentation.controller

import com.reservation.application.dto.ApiResponse
import com.reservation.application.dto.CreatePaymentRequest
import com.reservation.application.dto.PaymentResponse
import com.reservation.application.usecase.PaymentUseCase
import com.reservation.infrastructure.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val paymentUseCase: PaymentUseCase
) {

    @PostMapping
    fun createPayment(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @Valid @RequestBody request: CreatePaymentRequest
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val payment = paymentUseCase.createPayment(userDetails.id, request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(payment))
    }

    @GetMapping("/{paymentId}")
    fun getPayment(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable paymentId: Long
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val payment = paymentUseCase.getPayment(paymentId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(payment))
    }

    @GetMapping("/reservation/{reservationId}")
    fun getPaymentByReservation(
        @AuthenticationPrincipal userDetails: CustomUserDetails,
        @PathVariable reservationId: Long
    ): ResponseEntity<ApiResponse<PaymentResponse>> {
        val payment = paymentUseCase.getPaymentByReservation(reservationId, userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(payment))
    }
}
