package com.reservation.infrastructure.rabbitmq

import com.reservation.infrastructure.rabbitmq.message.NotificationMessage
import com.reservation.presentation.websocket.WebSocketNotificationService
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class NotificationMessageConsumer(
    private val webSocketNotificationService: WebSocketNotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @RabbitListener(queues = [RabbitMQConfig.NOTIFICATION_QUEUE])
    fun handleNotification(message: NotificationMessage) {
        logger.info("Received notification message: userId=${message.userId}, type=${message.type}")
        try {
            webSocketNotificationService.sendToUser(
                userId = message.userId,
                type = message.type.name,
                payload = mapOf(
                    "title" to message.title,
                    "message" to message.message,
                    "data" to (message.data ?: emptyMap<String, Any>())
                )
            )
            logger.info("Successfully sent notification: userId=${message.userId}")
        } catch (e: Exception) {
            logger.error("Failed to send notification: userId=${message.userId}", e)
        }
    }
}
