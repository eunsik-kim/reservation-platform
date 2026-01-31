package com.reservation.domain.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

enum class PaymentMethod {
    CARD, BANK_TRANSFER, VIRTUAL_ACCOUNT
}

@Entity
@Table(name = "payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    val reservation: Reservation,

    @Column(nullable = false, precision = 10, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var method: PaymentMethod = PaymentMethod.CARD,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PENDING,

    var transactionId: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var completedAt: LocalDateTime? = null
) {
    fun complete(transactionId: String) {
        this.status = PaymentStatus.COMPLETED
        this.transactionId = transactionId
        this.completedAt = LocalDateTime.now()
    }

    fun fail() {
        this.status = PaymentStatus.FAILED
    }

    fun refund() {
        this.status = PaymentStatus.REFUNDED
    }
}
