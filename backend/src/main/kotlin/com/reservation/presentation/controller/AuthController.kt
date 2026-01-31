package com.reservation.presentation.controller

import com.reservation.application.dto.*
import com.reservation.application.usecase.AuthUseCase
import com.reservation.infrastructure.security.CustomUserDetails
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authUseCase: AuthUseCase
) {

    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val user = authUseCase.signup(request)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(user))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val token = authUseCase.login(request)
        return ResponseEntity.ok(ApiResponse.success(token))
    }

    @PostMapping("/logout")
    fun logout(@RequestHeader("Authorization") authorization: String): ResponseEntity<ApiResponse<Unit>> {
        val token = authorization.removePrefix("Bearer ")
        authUseCase.logout(token)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<TokenResponse>> {
        val token = authUseCase.refresh(request)
        return ResponseEntity.ok(ApiResponse.success(token))
    }

    @GetMapping("/me")
    fun getCurrentUser(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<ApiResponse<UserResponse>> {
        val user = authUseCase.getCurrentUser(userDetails.id)
        return ResponseEntity.ok(ApiResponse.success(user))
    }
}
