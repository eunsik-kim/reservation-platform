package com.reservation.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "slots")
class Slot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var quantity: Int,

    @Column(nullable = false, precision = 10, scale = 2)
    var price: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    fun hasAvailableQuantity(): Boolean = quantity > 0

    fun decreaseQuantity(): Boolean {
        if (quantity > 0) {
            quantity--
            return true
        }
        return false
    }

    fun increaseQuantity() {
        quantity++
    }
}
