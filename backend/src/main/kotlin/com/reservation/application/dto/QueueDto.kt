package com.reservation.application.dto

import jakarta.validation.constraints.Positive

data class EnterQueueRequest(
    @field:Positive(message = "이벤트 ID는 필수입니다")
    val eventId: Long
)

data class QueuePositionResponse(
    val eventId: Long,
    val userId: Long,
    val position: Long,
    val totalInQueue: Long,
    val status: QueueStatus,
    val estimatedWaitTime: Long? = null // seconds
)

enum class QueueStatus {
    WAITING,    // 대기 중
    READY,      // 예약 가능
    EXPIRED,    // 만료됨
    NOT_IN_QUEUE // 대기열에 없음
}

data class QueueUpdateMessage(
    val eventId: Long,
    val userId: Long,
    val position: Long,
    val totalInQueue: Long,
    val status: QueueStatus,
    val message: String? = null
)
