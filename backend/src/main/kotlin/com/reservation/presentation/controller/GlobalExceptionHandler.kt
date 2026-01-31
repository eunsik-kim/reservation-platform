package com.reservation.presentation.controller

import com.reservation.application.dto.ApiResponse
import com.reservation.domain.exception.DomainException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Domain exception: ${e.errorCode} - ${e.message}")

        val status = when (e.errorCode) {
            "USER_NOT_FOUND", "EVENT_NOT_FOUND", "SLOT_NOT_FOUND",
            "RESERVATION_NOT_FOUND", "PAYMENT_NOT_FOUND" -> HttpStatus.NOT_FOUND
            "USER_ALREADY_EXISTS", "RESERVATION_ALREADY_EXISTS" -> HttpStatus.CONFLICT
            "UNAUTHORIZED", "INVALID_TOKEN" -> HttpStatus.UNAUTHORIZED
            "FORBIDDEN" -> HttpStatus.FORBIDDEN
            "EVENT_NOT_OPEN", "SLOT_SOLD_OUT", "RESERVATION_LIMIT_EXCEEDED",
            "QUEUE_NOT_READY", "PAYMENT_FAILED" -> HttpStatus.BAD_REQUEST
            else -> HttpStatus.INTERNAL_SERVER_ERROR
        }

        return ResponseEntity
            .status(status)
            .body(ApiResponse.error(e.errorCode, e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Unit>> {
        val errors = e.bindingResult.allErrors.map { error ->
            val fieldName = (error as? FieldError)?.field ?: "unknown"
            "$fieldName: ${error.defaultMessage}"
        }.joinToString(", ")

        logger.warn("Validation exception: $errors")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", errors))
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(e: BadCredentialsException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Bad credentials: ${e.message}")

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("UNAUTHORIZED", "이메일 또는 비밀번호가 올바르지 않습니다"))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Illegal argument: ${e.message}")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("BAD_REQUEST", e.message ?: "잘못된 요청입니다"))
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ApiResponse<Unit>> {
        logger.warn("Illegal state: ${e.message}")

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("CONFLICT", e.message ?: "요청을 처리할 수 없습니다"))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("Unexpected exception", e)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_ERROR", "서버 오류가 발생했습니다"))
    }
}
