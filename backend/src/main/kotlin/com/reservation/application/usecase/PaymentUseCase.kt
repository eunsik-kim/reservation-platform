package com.reservation.application.usecase

import com.reservation.application.dto.CreatePaymentRequest
import com.reservation.application.dto.PaymentResponse
import com.reservation.domain.entity.Payment
import com.reservation.domain.entity.PaymentMethod
import com.reservation.domain.entity.PaymentStatus
import com.reservation.domain.exception.ForbiddenException
import com.reservation.domain.exception.PaymentFailedException
import com.reservation.domain.exception.PaymentNotFoundException
import com.reservation.domain.exception.ReservationNotFoundException
import com.reservation.domain.repository.PaymentRepository
import com.reservation.domain.repository.ReservationRepository
import com.reservation.infrastructure.payment.MockPaymentAdapter
import com.reservation.infrastructure.rabbitmq.ReservationMessageProducer
import com.reservation.infrastructure.rabbitmq.message.NotificationMessage
import com.reservation.infrastructure.rabbitmq.message.NotificationType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class PaymentUseCase(
    private val paymentRepository: PaymentRepository,
    private val reservationRepository: ReservationRepository,
    private val mockPaymentAdapter: MockPaymentAdapter,
    private val messageProducer: ReservationMessageProducer
) {

    @Transactional
    fun createPayment(userId: Long, request: CreatePaymentRequest): PaymentResponse {
        val reservation = reservationRepository.findById(request.reservationId)
            .orElseThrow { ReservationNotFoundException() }

        if (reservation.userId != userId) {
            throw ForbiddenException("결제 권한이 없습니다")
        }

        // 이미 결제가 있는지 확인
        if (paymentRepository.existsByReservationId(request.reservationId)) {
            val existingPayment = paymentRepository.findByReservationId(request.reservationId)
                .orElseThrow { PaymentNotFoundException() }
            return PaymentResponse.from(existingPayment)
        }

        val amount = reservation.slot?.price ?: BigDecimal.ZERO

        val payment = Payment(
            reservation = reservation,
            amount = amount,
            method = request.method,
            status = PaymentStatus.PENDING
        )

        val savedPayment = paymentRepository.save(payment)

        // 결제 처리 (Mock)
        return processPaymentInternal(savedPayment.id, userId, amount, request.method.name)
    }

    @Transactional
    fun processPayment(reservationId: Long, userId: Long, amount: BigDecimal, method: String): PaymentResponse {
        val payment = paymentRepository.findByReservationId(reservationId)
            .orElseThrow { PaymentNotFoundException() }

        return processPaymentInternal(payment.id, userId, amount, method)
    }

    private fun processPaymentInternal(paymentId: Long, userId: Long, amount: BigDecimal, method: String): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException() }

        val result = mockPaymentAdapter.processPayment(amount, PaymentMethod.valueOf(method))

        if (result.success) {
            payment.complete(result.transactionId!!)
            val savedPayment = paymentRepository.save(payment)

            messageProducer.sendNotification(
                NotificationMessage(
                    userId = userId,
                    type = NotificationType.PAYMENT_COMPLETED,
                    title = "결제 완료",
                    message = "결제가 완료되었습니다. 금액: ${amount}원",
                    data = mapOf("paymentId" to savedPayment.id)
                )
            )

            return PaymentResponse.from(savedPayment)
        } else {
            payment.fail()
            paymentRepository.save(payment)

            messageProducer.sendNotification(
                NotificationMessage(
                    userId = userId,
                    type = NotificationType.PAYMENT_FAILED,
                    title = "결제 실패",
                    message = "결제에 실패했습니다. 다시 시도해주세요.",
                    data = mapOf("paymentId" to payment.id)
                )
            )

            throw PaymentFailedException()
        }
    }

    @Transactional(readOnly = true)
    fun getPayment(paymentId: Long, userId: Long): PaymentResponse {
        val payment = paymentRepository.findById(paymentId)
            .orElseThrow { PaymentNotFoundException() }

        if (payment.reservation.userId != userId) {
            throw ForbiddenException("결제 조회 권한이 없습니다")
        }

        return PaymentResponse.from(payment)
    }

    @Transactional(readOnly = true)
    fun getPaymentByReservation(reservationId: Long, userId: Long): PaymentResponse {
        val payment = paymentRepository.findByReservationId(reservationId)
            .orElseThrow { PaymentNotFoundException() }

        if (payment.reservation.userId != userId) {
            throw ForbiddenException("결제 조회 권한이 없습니다")
        }

        return PaymentResponse.from(payment)
    }
}
