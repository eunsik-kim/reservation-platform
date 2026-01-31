package com.reservation.application.dto

import com.reservation.domain.entity.Reservation
import com.reservation.domain.entity.ReservationStatus
import jakarta.validation.constraints.Positive
import java.time.LocalDateTime

data class CreateReservationRequest(
    @field:Positive(message = "이벤트 ID는 필수입니다")
    val eventId: Long,

    val slotId: Long? = null
)

data class ReservationResponse(
    val id: Long,
    val eventId: Long,
    val eventTitle: String,
    val userId: Long,
    val slotId: Long?,
    val slotName: String?,
    val status: ReservationStatus,
    val createdAt: LocalDateTime,
    val confirmedAt: LocalDateTime?
) {
    companion object {
        fun from(reservation: Reservation): ReservationResponse {
            return ReservationResponse(
                id = reservation.id,
                eventId = reservation.event.id,
                eventTitle = reservation.event.title,
                userId = reservation.userId,
                slotId = reservation.slot?.id,
                slotName = reservation.slot?.name,
                status = reservation.status,
                createdAt = reservation.createdAt,
                confirmedAt = reservation.confirmedAt
            )
        }
    }
}

data class ReservationListResponse(
    val reservations: List<ReservationResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

data class ParticipantResponse(
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val reservationId: Long,
    val slotName: String?,
    val status: ReservationStatus,
    val reservedAt: LocalDateTime
)

data class ParticipantListResponse(
    val participants: List<ParticipantResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)
