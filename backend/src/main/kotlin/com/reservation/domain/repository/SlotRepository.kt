package com.reservation.domain.repository

import com.reservation.domain.entity.Slot
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import jakarta.persistence.LockModeType
import java.util.Optional

@Repository
interface SlotRepository : JpaRepository<Slot, Long> {
    fun findByEventId(eventId: Long): List<Slot>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId")
    fun findByIdWithLock(slotId: Long): Optional<Slot>
}
