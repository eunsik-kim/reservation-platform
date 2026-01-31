package com.reservation.domain.repository

import com.reservation.domain.entity.Payment
import com.reservation.domain.entity.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByReservationId(reservationId: Long): Optional<Payment>
    fun findByStatus(status: PaymentStatus): List<Payment>
    fun existsByReservationId(reservationId: Long): Boolean
}
