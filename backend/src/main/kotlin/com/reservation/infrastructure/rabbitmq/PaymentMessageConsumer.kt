package com.reservation.infrastructure.rabbitmq

import com.reservation.application.usecase.PaymentUseCase
import com.reservation.infrastructure.rabbitmq.message.PaymentMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class PaymentMessageConsumer(
    private val paymentUseCase: PaymentUseCase
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitMQConfig.PAYMENT_QUEUE])
    fun handlePayment(message: PaymentMessage) {
        logger.info("Received payment message: reservationId=${message.reservationId}")
        try {
            paymentUseCase.processPayment(
                reservationId = message.reservationId,
                userId = message.userId,
                amount = message.amount,
                method = message.method
            )
            logger.info("Successfully processed payment: reservationId=${message.reservationId}")
        } catch (e: Exception) {
            logger.error("Failed to process payment: reservationId=${message.reservationId}", e)
            throw e
        }
    }
}
