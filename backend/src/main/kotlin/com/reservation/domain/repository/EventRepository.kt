package com.reservation.domain.repository

import com.reservation.domain.entity.Event
import com.reservation.domain.entity.EventStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<Event, Long> {
    fun findByCreatorId(creatorId: Long, pageable: Pageable): Page<Event>
    fun findByCreatorId(creatorId: Long): List<Event>

    fun findByStatus(status: EventStatus, pageable: Pageable): Page<Event>

    @Query("SELECT e FROM Event e WHERE e.status IN :statuses ORDER BY e.openAt ASC")
    fun findByStatusIn(statuses: List<EventStatus>, pageable: Pageable): Page<Event>

    @Query("SELECT e FROM Event e WHERE e.status = 'SCHEDULED' AND e.openAt <= :now")
    fun findEventsToOpen(now: LocalDateTime): List<Event>

    @Query("SELECT e FROM Event e WHERE e.status = 'OPEN' AND e.closeAt <= :now")
    fun findEventsToClose(now: LocalDateTime): List<Event>
}
