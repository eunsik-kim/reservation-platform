package com.reservation.infrastructure.rabbitmq

import com.reservation.application.usecase.ReservationUseCase
import com.reservation.infrastructure.rabbitmq.message.CancelReservationMessage
import com.reservation.infrastructure.rabbitmq.message.CreateReservationMessage
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class ReservationMessageConsumer(
    private val reservationUseCase: ReservationUseCase
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitMQConfig.CREATE_QUEUE])
    fun handleCreateReservation(message: CreateReservationMessage) {
        logger.info("Received create reservation message: eventId=${message.eventId}, userId=${message.userId}")
        try {
            reservationUseCase.processReservation(
                eventId = message.eventId,
                userId = message.userId,
                slotId = message.slotId
            )
            logger.info("Successfully processed reservation: eventId=${message.eventId}, userId=${message.userId}")
        } catch (e: Exception) {
            logger.error("Failed to process reservation: eventId=${message.eventId}, userId=${message.userId}", e)
            throw e
        }
    }

    @RabbitListener(queues = [RabbitMQConfig.CANCEL_QUEUE])
    fun handleCancelReservation(message: CancelReservationMessage) {
        logger.info("Received cancel reservation message: reservationId=${message.reservationId}")
        try {
            reservationUseCase.cancelReservation(
                reservationId = message.reservationId,
                userId = message.userId
            )
            logger.info("Successfully cancelled reservation: reservationId=${message.reservationId}")
        } catch (e: Exception) {
            logger.error("Failed to cancel reservation: reservationId=${message.reservationId}", e)
            throw e
        }
    }
}
