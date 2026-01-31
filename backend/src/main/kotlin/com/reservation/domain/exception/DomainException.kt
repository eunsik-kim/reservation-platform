package com.reservation.domain.exception

open class DomainException(
    override val message: String,
    val errorCode: String
) : RuntimeException(message)

class UserNotFoundException(message: String = "사용자를 찾을 수 없습니다") :
    DomainException(message, "USER_NOT_FOUND")

class UserAlreadyExistsException(message: String = "이미 존재하는 이메일입니다") :
    DomainException(message, "USER_ALREADY_EXISTS")

class EventNotFoundException(message: String = "이벤트를 찾을 수 없습니다") :
    DomainException(message, "EVENT_NOT_FOUND")

class EventNotOpenException(message: String = "예약이 열리지 않은 이벤트입니다") :
    DomainException(message, "EVENT_NOT_OPEN")

class SlotNotFoundException(message: String = "슬롯을 찾을 수 없습니다") :
    DomainException(message, "SLOT_NOT_FOUND")

class SlotSoldOutException(message: String = "매진된 슬롯입니다") :
    DomainException(message, "SLOT_SOLD_OUT")

class ReservationNotFoundException(message: String = "예약을 찾을 수 없습니다") :
    DomainException(message, "RESERVATION_NOT_FOUND")

class ReservationLimitExceededException(message: String = "예약 가능 수량을 초과했습니다") :
    DomainException(message, "RESERVATION_LIMIT_EXCEEDED")

class ReservationAlreadyExistsException(message: String = "이미 예약이 존재합니다") :
    DomainException(message, "RESERVATION_ALREADY_EXISTS")

class PaymentNotFoundException(message: String = "결제를 찾을 수 없습니다") :
    DomainException(message, "PAYMENT_NOT_FOUND")

class PaymentFailedException(message: String = "결제에 실패했습니다") :
    DomainException(message, "PAYMENT_FAILED")

class UnauthorizedException(message: String = "인증이 필요합니다") :
    DomainException(message, "UNAUTHORIZED")

class ForbiddenException(message: String = "권한이 없습니다") :
    DomainException(message, "FORBIDDEN")

class QueueNotReadyException(message: String = "아직 예약 차례가 아닙니다") :
    DomainException(message, "QUEUE_NOT_READY")

class InvalidTokenException(message: String = "유효하지 않은 토큰입니다") :
    DomainException(message, "INVALID_TOKEN")
