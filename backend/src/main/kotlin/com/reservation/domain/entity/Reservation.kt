package com.reservation.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, EXPIRED
}

@Entity
@Table(name = "reservations")
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    val slot: Slot? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var confirmedAt: LocalDateTime? = null,

    var cancelledAt: LocalDateTime? = null
) {
    fun confirm() {
        status = ReservationStatus.CONFIRMED
        confirmedAt = LocalDateTime.now()
    }

    fun cancel() {
        status = ReservationStatus.CANCELLED
        cancelledAt = LocalDateTime.now()
    }

    fun expire() {
        status = ReservationStatus.EXPIRED
    }

    fun isPending(): Boolean = status == ReservationStatus.PENDING
    fun isConfirmed(): Boolean = status == ReservationStatus.CONFIRMED
}
