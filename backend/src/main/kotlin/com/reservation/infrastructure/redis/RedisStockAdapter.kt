package com.reservation.infrastructure.redis

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisStockAdapter(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val STOCK_PREFIX = "stock:slot:"
        private const val EVENT_STOCK_PREFIX = "stock:event:"
    }

    /**
     * 슬롯 재고 초기화
     */
    fun initializeSlotStock(slotId: Long, quantity: Int) {
        val key = "$STOCK_PREFIX$slotId"
        redisTemplate.opsForValue().set(key, quantity.toString())
    }

    /**
     * 이벤트 전체 재고 초기화 (슬롯 없는 경우)
     */
    fun initializeEventStock(eventId: Long, quantity: Int) {
        val key = "$EVENT_STOCK_PREFIX$eventId"
        redisTemplate.opsForValue().set(key, quantity.toString())
    }

    /**
     * 슬롯 재고 감소 (원자적 연산)
     * 반환값: 감소 후 재고 수량. 0 미만이면 실패로 간주하고 롤백
     */
    fun decrementSlotStock(slotId: Long): Long {
        val key = "$STOCK_PREFIX$slotId"
        val remaining = redisTemplate.opsForValue().decrement(key)
        if (remaining != null && remaining < 0) {
            redisTemplate.opsForValue().increment(key)
            return -1
        }
        return remaining ?: -1
    }

    /**
     * 이벤트 재고 감소
     */
    fun decrementEventStock(eventId: Long): Long {
        val key = "$EVENT_STOCK_PREFIX$eventId"
        val remaining = redisTemplate.opsForValue().decrement(key)
        if (remaining != null && remaining < 0) {
            redisTemplate.opsForValue().increment(key)
            return -1
        }
        return remaining ?: -1
    }

    /**
     * 슬롯 재고 증가 (예약 취소 시)
     */
    fun incrementSlotStock(slotId: Long): Long {
        val key = "$STOCK_PREFIX$slotId"
        return redisTemplate.opsForValue().increment(key) ?: 0
    }

    /**
     * 이벤트 재고 증가
     */
    fun incrementEventStock(eventId: Long): Long {
        val key = "$EVENT_STOCK_PREFIX$eventId"
        return redisTemplate.opsForValue().increment(key) ?: 0
    }

    /**
     * 슬롯 재고 조회
     */
    fun getSlotStock(slotId: Long): Long {
        val key = "$STOCK_PREFIX$slotId"
        return redisTemplate.opsForValue().get(key)?.toLongOrNull() ?: 0
    }

    /**
     * 이벤트 재고 조회
     */
    fun getEventStock(eventId: Long): Long {
        val key = "$EVENT_STOCK_PREFIX$eventId"
        return redisTemplate.opsForValue().get(key)?.toLongOrNull() ?: 0
    }
}
