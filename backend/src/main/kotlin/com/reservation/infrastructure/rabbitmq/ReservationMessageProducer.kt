package com.reservation.infrastructure.rabbitmq

import com.reservation.infrastructure.rabbitmq.message.*
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class ReservationMessageProducer(
    private val rabbitTemplate: RabbitTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendCreateReservation(message: CreateReservationMessage) {
        logger.info("Sending create reservation message: eventId=${message.eventId}, userId=${message.userId}")
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.CREATE_ROUTING_KEY,
            message
        )
    }

    fun sendCancelReservation(message: CancelReservationMessage) {
        logger.info("Sending cancel reservation message: reservationId=${message.reservationId}")
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.CANCEL_ROUTING_KEY,
            message
        )
    }

    fun sendPayment(message: PaymentMessage) {
        logger.info("Sending payment message: reservationId=${message.reservationId}")
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.PAYMENT_ROUTING_KEY,
            message
        )
    }

    fun sendNotification(message: NotificationMessage) {
        logger.info("Sending notification message: userId=${message.userId}, type=${message.type}")
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_NAME,
            RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
            message
        )
    }
}
