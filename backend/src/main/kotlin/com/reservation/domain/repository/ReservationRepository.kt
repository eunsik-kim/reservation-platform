package com.reservation.domain.repository

import com.reservation.domain.entity.Reservation
import com.reservation.domain.entity.ReservationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    fun findByUserId(userId: Long, pageable: Pageable): Page<Reservation>
    fun findByUserId(userId: Long): List<Reservation>

    fun findByEventId(eventId: Long): List<Reservation>
    fun findByEventId(eventId: Long, pageable: Pageable): Page<Reservation>

    fun findByEventIdAndUserId(eventId: Long, userId: Long): List<Reservation>

    fun findByEventIdAndStatus(eventId: Long, status: ReservationStatus): List<Reservation>

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.event.id = :eventId AND r.status IN ('PENDING', 'CONFIRMED')")
    fun countActiveReservationsByEventId(eventId: Long): Long

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.event.id = :eventId AND r.userId = :userId AND r.status IN ('PENDING', 'CONFIRMED')")
    fun countActiveReservationsByEventIdAndUserId(eventId: Long, userId: Long): Long

    fun existsByEventIdAndUserIdAndStatusIn(eventId: Long, userId: Long, statuses: List<ReservationStatus>): Boolean
}
