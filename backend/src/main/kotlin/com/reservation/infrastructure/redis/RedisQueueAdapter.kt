package com.reservation.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RedisQueueAdapter(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val QUEUE_PREFIX = "queue:event:"
        private const val READY_PREFIX = "ready:event:"
        private const val QUEUE_STATUS_PREFIX = "queue:status:"
    }

    /**
     * 대기열에 사용자 추가 (ZADD)
     * Score는 timestamp로 설정하여 선착순 보장
     */
    fun enterQueue(eventId: Long, userId: Long): Long {
        val key = "$QUEUE_PREFIX$eventId"
        val score = Instant.now().toEpochMilli().toDouble()
        redisTemplate.opsForZSet().add(key, userId.toString(), score)
        return getPosition(eventId, userId) ?: -1
    }

    /**
     * 대기열에서 사용자 제거 (ZREM)
     */
    fun leaveQueue(eventId: Long, userId: Long): Boolean {
        val key = "$QUEUE_PREFIX$eventId"
        val removed = redisTemplate.opsForZSet().remove(key, userId.toString())
        return removed != null && removed > 0
    }

    /**
     * 대기열 순위 조회 (ZRANK)
     * 0-indexed이므로 +1해서 반환
     */
    fun getPosition(eventId: Long, userId: Long): Long? {
        val key = "$QUEUE_PREFIX$eventId"
        val rank = redisTemplate.opsForZSet().rank(key, userId.toString())
        return rank?.plus(1)
    }

    /**
     * 대기열 전체 크기 조회
     */
    fun getQueueSize(eventId: Long): Long {
        val key = "$QUEUE_PREFIX$eventId"
        return redisTemplate.opsForZSet().size(key) ?: 0
    }

    /**
     * 상위 N명 대기열에서 가져오기 (ZPOPMIN)
     */
    fun popTopUsers(eventId: Long, count: Long): List<Long> {
        val key = "$QUEUE_PREFIX$eventId"
        val results = redisTemplate.opsForZSet().popMin(key, count)
        return results?.mapNotNull { it.value?.toLongOrNull() } ?: emptyList()
    }

    /**
     * 예약 가능 상태 사용자 추가
     */
    fun addToReadySet(eventId: Long, userId: Long, expirationSeconds: Long) {
        val key = "$READY_PREFIX$eventId:$userId"
        redisTemplate.opsForValue().set(key, "ready")
        redisTemplate.expire(key, java.time.Duration.ofSeconds(expirationSeconds))
    }

    /**
     * 예약 가능 상태 확인
     */
    fun isReady(eventId: Long, userId: Long): Boolean {
        val key = "$READY_PREFIX$eventId:$userId"
        return redisTemplate.hasKey(key)
    }

    /**
     * 예약 완료 후 ready 상태 제거
     */
    fun removeFromReadySet(eventId: Long, userId: Long) {
        val key = "$READY_PREFIX$eventId:$userId"
        redisTemplate.delete(key)
    }

    /**
     * 사용자가 대기열에 있는지 확인
     */
    fun isInQueue(eventId: Long, userId: Long): Boolean {
        val key = "$QUEUE_PREFIX$eventId"
        return redisTemplate.opsForZSet().rank(key, userId.toString()) != null
    }

    /**
     * 대기열 전체 삭제 (이벤트 종료 시)
     */
    fun clearQueue(eventId: Long) {
        val key = "$QUEUE_PREFIX$eventId"
        redisTemplate.delete(key)
    }

    /**
     * 대기열 상위 N명 조회 (제거하지 않음)
     */
    fun peekTopUsers(eventId: Long, count: Long): List<Long> {
        val key = "$QUEUE_PREFIX$eventId"
        val results = redisTemplate.opsForZSet().range(key, 0, count - 1)
        return results?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    /**
     * 사용자 대기열 상태 저장 (WAITING, READY, RESERVED)
     */
    fun setUserQueueStatus(eventId: Long, userId: Long, status: String) {
        val key = "$QUEUE_STATUS_PREFIX$eventId:$userId"
        redisTemplate.opsForValue().set(key, status)
    }

    /**
     * 사용자 대기열 상태 조회
     */
    fun getUserQueueStatus(eventId: Long, userId: Long): String? {
        val key = "$QUEUE_STATUS_PREFIX$eventId:$userId"
        return redisTemplate.opsForValue().get(key)
    }

    /**
     * 사용자 대기열 상태 삭제
     */
    fun removeUserQueueStatus(eventId: Long, userId: Long) {
        val key = "$QUEUE_STATUS_PREFIX$eventId:$userId"
        redisTemplate.delete(key)
    }
}
