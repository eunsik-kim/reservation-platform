package com.reservation.application.dto

import com.reservation.domain.entity.Event
import com.reservation.domain.entity.EventStatus
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateEventRequest(
    @field:NotBlank(message = "제목은 필수입니다")
    val title: String,

    val description: String? = null,

    @field:Future(message = "오픈 시간은 현재 시간 이후여야 합니다")
    val openAt: LocalDateTime,

    @field:Future(message = "마감 시간은 현재 시간 이후여야 합니다")
    val closeAt: LocalDateTime,

    @field:Positive(message = "최대 참가자 수는 1 이상이어야 합니다")
    val maxParticipants: Int,

    val settings: EventSettings = EventSettings(),

    val slots: List<CreateSlotRequest>? = null
)

data class UpdateEventRequest(
    val title: String? = null,
    val description: String? = null,
    val openAt: LocalDateTime? = null,
    val closeAt: LocalDateTime? = null,
    val maxParticipants: Int? = null,
    val status: EventStatus? = null,
    val settings: EventSettings? = null
)

data class CreateSlotRequest(
    @field:NotBlank(message = "슬롯 이름은 필수입니다")
    val name: String,

    @field:Positive(message = "수량은 1 이상이어야 합니다")
    val quantity: Int,

    val price: BigDecimal = BigDecimal.ZERO
)

data class EventSettings(
    val useQueue: Boolean = true,
    val queueBatchSize: Int = 100,
    val requirePayment: Boolean = false,
    val maxReservationsPerUser: Int = 1,
    val reservationTimeLimit: Int = 600
)

data class EventResponse(
    val id: Long,
    val creatorId: Long,
    val title: String,
    val description: String?,
    val openAt: LocalDateTime,
    val closeAt: LocalDateTime,
    val maxParticipants: Int,
    val status: EventStatus,
    val settings: EventSettings,
    val currentParticipants: Int,
    val slots: List<SlotResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(event: Event, currentParticipants: Int = 0): EventResponse {
            return EventResponse(
                id = event.id,
                creatorId = event.creatorId,
                title = event.title,
                description = event.description,
                openAt = event.openAt,
                closeAt = event.closeAt,
                maxParticipants = event.maxParticipants,
                status = event.status,
                settings = parseSettings(event.settings),
                currentParticipants = currentParticipants,
                slots = event.slots.map { SlotResponse.from(it) },
                createdAt = event.createdAt,
                updatedAt = event.updatedAt
            )
        }

        private fun parseSettings(settingsJson: String): EventSettings {
            return try {
                com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
                    .readValue(settingsJson, EventSettings::class.java)
            } catch (e: Exception) {
                EventSettings()
            }
        }
    }
}

data class EventListResponse(
    val events: List<EventResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int
)

data class SlotResponse(
    val id: Long,
    val name: String,
    val quantity: Int,
    val price: BigDecimal,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(slot: com.reservation.domain.entity.Slot): SlotResponse {
            return SlotResponse(
                id = slot.id,
                name = slot.name,
                quantity = slot.quantity,
                price = slot.price,
                createdAt = slot.createdAt
            )
        }
    }
}
