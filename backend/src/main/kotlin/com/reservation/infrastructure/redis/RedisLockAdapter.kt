package com.reservation.infrastructure.redis

import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class RedisLockAdapter(
    private val redissonClient: RedissonClient
) {
    companion object {
        private const val LOCK_PREFIX = "lock:"
        private const val DEFAULT_WAIT_TIME = 5L
        private const val DEFAULT_LEASE_TIME = 10L
    }

    /**
     * 분산 락 획득
     */
    fun <T> executeWithLock(
        lockKey: String,
        waitTime: Long = DEFAULT_WAIT_TIME,
        leaseTime: Long = DEFAULT_LEASE_TIME,
        action: () -> T
    ): T {
        val lock = redissonClient.getLock("$LOCK_PREFIX$lockKey")
        val acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)

        if (!acquired) {
            throw IllegalStateException("Failed to acquire lock for key: $lockKey")
        }

        return try {
            action()
        } finally {
            if (lock.isHeldByCurrentThread) {
                lock.unlock()
            }
        }
    }

    /**
     * 슬롯 예약용 락
     */
    fun <T> executeWithSlotLock(slotId: Long, action: () -> T): T {
        return executeWithLock("slot:$slotId", action = action)
    }

    /**
     * 이벤트 예약용 락
     */
    fun <T> executeWithEventLock(eventId: Long, action: () -> T): T {
        return executeWithLock("event:$eventId", action = action)
    }

    /**
     * 사용자별 예약 중복 방지 락
     */
    fun <T> executeWithUserReservationLock(eventId: Long, userId: Long, action: () -> T): T {
        return executeWithLock("reservation:$eventId:$userId", action = action)
    }
}
