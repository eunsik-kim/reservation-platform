package com.reservation.application.usecase

import com.reservation.application.dto.*
import com.reservation.domain.entity.User
import com.reservation.domain.entity.UserRole
import com.reservation.domain.exception.InvalidTokenException
import com.reservation.domain.exception.UnauthorizedException
import com.reservation.domain.exception.UserAlreadyExistsException
import com.reservation.domain.exception.UserNotFoundException
import com.reservation.domain.repository.UserRepository
import com.reservation.infrastructure.security.JwtTokenProvider
import com.reservation.infrastructure.security.TokenBlacklistService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenBlacklistService: TokenBlacklistService
) {

    @Transactional
    fun signup(request: SignupRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw UserAlreadyExistsException()
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            role = UserRole.USER
        )

        val savedUser = userRepository.save(user)
        return UserResponse(
            id = savedUser.id,
            email = savedUser.email,
            name = savedUser.name,
            role = savedUser.role.name
        )
    }

    fun login(request: LoginRequest): TokenResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UserNotFoundException("이메일 또는 비밀번호가 올바르지 않습니다") }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다")
        }

        val accessToken = jwtTokenProvider.createAccessToken(user.id, user.email, user.role.name)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email, user.role.name)

        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = 3600
        )
    }

    fun logout(accessToken: String) {
        if (jwtTokenProvider.validateToken(accessToken)) {
            val expiration = jwtTokenProvider.getExpiration(accessToken)
            val remainingTime = expiration.time - System.currentTimeMillis()
            if (remainingTime > 0) {
                tokenBlacklistService.addToBlacklist(accessToken, remainingTime)
            }
        }
    }

    fun refresh(request: RefreshTokenRequest): TokenResponse {
        val refreshToken = request.refreshToken

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw InvalidTokenException("유효하지 않은 리프레시 토큰입니다")
        }

        val tokenType = jwtTokenProvider.getTokenType(refreshToken)
        if (tokenType != "refresh") {
            throw InvalidTokenException("리프레시 토큰이 아닙니다")
        }

        val userId = jwtTokenProvider.getUserId(refreshToken)
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        val newAccessToken = jwtTokenProvider.createAccessToken(user.id, user.email, user.role.name)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(user.id, user.email, user.role.name)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            expiresIn = 3600
        )
    }

    fun getCurrentUser(userId: Long): UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }

        return UserResponse(
            id = user.id,
            email = user.email,
            name = user.name,
            role = user.role.name
        )
    }
}
