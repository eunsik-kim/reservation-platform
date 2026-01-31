package com.reservation.infrastructure.payment

import com.reservation.domain.entity.PaymentMethod
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.*
import kotlin.random.Random

@Component
class MockPaymentAdapter {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun processPayment(amount: BigDecimal, method: PaymentMethod): PaymentResult {
        logger.info("Processing mock payment: amount=$amount, method=$method")

        // 랜덤 딜레이 시뮬레이션 (100ms ~ 500ms)
        Thread.sleep(Random.nextLong(100, 500))

        // 95% 성공률 시뮬레이션
        val isSuccess = Random.nextDouble() < 0.95

        return if (isSuccess) {
            val transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}"
            logger.info("Payment successful: transactionId=$transactionId")
            PaymentResult(
                success = true,
                transactionId = transactionId,
                message = "결제가 완료되었습니다"
            )
        } else {
            logger.warn("Payment failed: simulated failure")
            PaymentResult(
                success = false,
                transactionId = null,
                message = "결제에 실패했습니다. 잠시 후 다시 시도해주세요."
            )
        }
    }
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val message: String
)
