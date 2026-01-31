package com.reservation.presentation.websocket

import com.reservation.application.usecase.QueueUseCase
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class QueueWebSocketHandler(
    private val queueUseCase: QueueUseCase,
    private val webSocketNotificationService: WebSocketNotificationService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @MessageMapping("/queue/{eventId}/enter")
    @SendTo("/topic/queue/{eventId}")
    fun enterQueue(
        @DestinationVariable eventId: Long,
        headerAccessor: SimpMessageHeaderAccessor
    ): Map<String, Any> {
        val userId = extractUserId(headerAccessor)
        logger.info("User $userId entering queue for event $eventId")

        return try {
            val position = queueUseCase.enterQueue(eventId, userId)
            mapOf(
                "success" to true,
                "data" to position
            )
        } catch (e: Exception) {
            logger.error("Failed to enter queue", e)
            mapOf(
                "success" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }

    @MessageMapping("/queue/{eventId}/position")
    fun getPosition(
        @DestinationVariable eventId: Long,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val userId = extractUserId(headerAccessor)
        logger.info("User $userId checking position for event $eventId")

        try {
            val position = queueUseCase.getPosition(eventId, userId)
            webSocketNotificationService.sendToUser(userId, "QUEUE_POSITION", position)
        } catch (e: Exception) {
            logger.error("Failed to get queue position", e)
            webSocketNotificationService.sendToUser(userId, "ERROR", mapOf("message" to e.message))
        }
    }

    @MessageMapping("/queue/{eventId}/leave")
    fun leaveQueue(
        @DestinationVariable eventId: Long,
        headerAccessor: SimpMessageHeaderAccessor
    ) {
        val userId = extractUserId(headerAccessor)
        logger.info("User $userId leaving queue for event $eventId")

        try {
            queueUseCase.leaveQueue(eventId, userId)
            webSocketNotificationService.sendToUser(userId, "QUEUE_LEFT", mapOf("eventId" to eventId))
        } catch (e: Exception) {
            logger.error("Failed to leave queue", e)
            webSocketNotificationService.sendToUser(userId, "ERROR", mapOf("message" to e.message))
        }
    }

    private fun extractUserId(headerAccessor: SimpMessageHeaderAccessor): Long {
        // 실제 구현에서는 JWT 토큰에서 추출하거나 세션에서 가져옴
        val userIdHeader = headerAccessor.getFirstNativeHeader("userId")
        return userIdHeader?.toLongOrNull()
            ?: throw IllegalArgumentException("User ID not found in header")
    }
}
