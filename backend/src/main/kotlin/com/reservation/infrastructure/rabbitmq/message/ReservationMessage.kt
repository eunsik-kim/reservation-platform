package com.reservation.infrastructure.rabbitmq.message

import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateReservationMessage(
    val eventId: Long,
    val userId: Long,
    val slotId: Long? = null,
    val requestedAt: LocalDateTime = LocalDateTime.now()
) : Serializable

data class CancelReservationMessage(
    val reservationId: Long,
    val userId: Long,
    val reason: String? = null,
    val requestedAt: LocalDateTime = LocalDateTime.now()
) : Serializable

data class PaymentMessage(
    val reservationId: Long,
    val userId: Long,
    val amount: BigDecimal,
    val method: String,
    val requestedAt: LocalDateTime = LocalDateTime.now()
) : Serializable

data class NotificationMessage(
    val userId: Long,
    val type: NotificationType,
    val title: String,
    val message: String,
    val data: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
) : Serializable

enum class NotificationType {
    QUEUE_READY,           // 예약 차례 도착
    RESERVATION_CONFIRMED, // 예약 확정
    RESERVATION_CANCELLED, // 예약 취소
    PAYMENT_COMPLETED,     // 결제 완료
    PAYMENT_FAILED,        // 결제 실패
    EVENT_REMINDER         // 이벤트 리마인더
}
