package com.reservation.infrastructure.security

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TokenBlacklistService(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val BLACKLIST_PREFIX = "token:blacklist:"
    }

    fun addToBlacklist(token: String, expirationMs: Long) {
        val key = "$BLACKLIST_PREFIX$token"
        redisTemplate.opsForValue().set(key, "blacklisted", expirationMs, TimeUnit.MILLISECONDS)
    }

    fun isBlacklisted(token: String): Boolean {
        val key = "$BLACKLIST_PREFIX$token"
        return redisTemplate.hasKey(key)
    }
}
