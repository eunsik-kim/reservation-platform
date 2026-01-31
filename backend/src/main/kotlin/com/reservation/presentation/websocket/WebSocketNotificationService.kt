package com.reservation.presentation.websocket

import com.reservation.application.dto.QueueUpdateMessage
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class WebSocketNotificationService(
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 특정 사용자에게 메시지 전송
     */
    fun sendToUser(userId: Long, type: String, payload: Any) {
        val destination = "/queue/notifications"
        val message = mapOf(
            "type" to type,
            "payload" to payload
        )
        messagingTemplate.convertAndSendToUser(userId.toString(), destination, message)
        logger.debug("Sent message to user $userId: type=$type")
    }

    /**
     * 대기열 업데이트 전송
     */
    fun sendQueueUpdate(eventId: Long, userId: Long, message: QueueUpdateMessage) {
        sendToUser(userId, "QUEUE_UPDATE", message)
    }

    /**
     * 대기열 전체 브로드캐스트 (이벤트별)
     */
    fun broadcastQueueUpdate(eventId: Long, payload: Any) {
        val destination = "/topic/queue/$eventId"
        messagingTemplate.convertAndSend(destination, payload)
        logger.debug("Broadcast queue update for event $eventId")
    }

    /**
     * 예약 차례 알림
     */
    fun sendQueueReady(eventId: Long, userId: Long) {
        val message = mapOf(
            "eventId" to eventId,
            "status" to "READY",
            "message" to "예약 차례가 되었습니다. 예약을 진행해주세요."
        )
        sendToUser(userId, "QUEUE_READY", message)
    }
}
