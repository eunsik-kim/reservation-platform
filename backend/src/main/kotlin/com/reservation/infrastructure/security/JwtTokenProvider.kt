package com.reservation.infrastructure.security

import com.reservation.domain.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
    private val tokenBlacklistService: TokenBlacklistService
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun createAccessToken(userId: Long, email: String, role: String): String {
        return createToken(userId, email, role, jwtProperties.accessTokenExpiration, "access")
    }

    fun createRefreshToken(userId: Long, email: String, role: String): String {
        return createToken(userId, email, role, jwtProperties.refreshTokenExpiration, "refresh")
    }

    private fun createToken(userId: Long, email: String, role: String, expiration: Long, type: String): String {
        val now = Date()
        val validity = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role)
            .claim("type", type)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            if (tokenBlacklistService.isBlacklisted(token)) {
                return false
            }
            val claims = getClaims(token)
            !claims.expiration.before(Date())
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    fun getUserId(token: String): Long {
        return getClaims(token).subject.toLong()
    }

    fun getEmail(token: String): String {
        return getClaims(token)["email"] as String
    }

    fun getRole(token: String): String {
        return getClaims(token)["role"] as String
    }

    fun getTokenType(token: String): String {
        return getClaims(token)["type"] as String
    }

    fun getExpiration(token: String): Date {
        return getClaims(token).expiration
    }

    private fun getClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: Exception) {
            throw InvalidTokenException()
        }
    }
}
