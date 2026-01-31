package com.reservation.application.dto

import com.reservation.domain.entity.Payment
import com.reservation.domain.entity.PaymentMethod
import com.reservation.domain.entity.PaymentStatus
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreatePaymentRequest(
    @field:Positive(message = "예약 ID는 필수입니다")
    val reservationId: Long,

    val method: PaymentMethod = PaymentMethod.CARD
)

data class PaymentResponse(
    val id: Long,
    val reservationId: Long,
    val amount: BigDecimal,
    val method: PaymentMethod,
    val status: PaymentStatus,
    val transactionId: String?,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                id = payment.id,
                reservationId = payment.reservation.id,
                amount = payment.amount,
                method = payment.method,
                status = payment.status,
                transactionId = payment.transactionId,
                createdAt = payment.createdAt,
                completedAt = payment.completedAt
            )
        }
    }
}
