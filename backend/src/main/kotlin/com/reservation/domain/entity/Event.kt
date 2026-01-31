package com.reservation.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class EventStatus {
    DRAFT, SCHEDULED, OPEN, CLOSED, CANCELLED
}

@Entity
@Table(name = "events")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val creatorId: Long,

    @Column(nullable = false)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    var openAt: LocalDateTime,

    @Column(nullable = false)
    var closeAt: LocalDateTime,

    @Column(nullable = false)
    var maxParticipants: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EventStatus = EventStatus.DRAFT,

    @Column(columnDefinition = "jsonb")
    var settings: String = """{"useQueue":true,"queueBatchSize":100,"requirePayment":false,"maxReservationsPerUser":1,"reservationTimeLimit":600}""",

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true)
    val slots: MutableList<Slot> = mutableListOf(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL])
    val reservations: MutableList<Reservation> = mutableListOf()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun isOpen(): Boolean = status == EventStatus.OPEN

    fun canReserve(): Boolean {
        val now = LocalDateTime.now()
        return status == EventStatus.OPEN && now.isAfter(openAt) && now.isBefore(closeAt)
    }
}
