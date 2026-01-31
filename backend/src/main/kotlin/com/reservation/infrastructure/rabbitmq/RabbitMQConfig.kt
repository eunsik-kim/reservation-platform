package com.reservation.infrastructure.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMQConfig(
    private val objectMapper: ObjectMapper
) {
    companion object {
        const val EXCHANGE_NAME = "reservation.exchange"
        const val CREATE_QUEUE = "reservation.create.queue"
        const val CANCEL_QUEUE = "reservation.cancel.queue"
        const val PAYMENT_QUEUE = "reservation.payment.queue"
        const val NOTIFICATION_QUEUE = "reservation.notification.queue"

        const val CREATE_ROUTING_KEY = "reservation.create"
        const val CANCEL_ROUTING_KEY = "reservation.cancel"
        const val PAYMENT_ROUTING_KEY = "reservation.payment"
        const val NOTIFICATION_ROUTING_KEY = "reservation.notification"
    }

    @Bean
    fun reservationExchange(): DirectExchange {
        return DirectExchange(EXCHANGE_NAME)
    }

    @Bean
    fun createQueue(): Queue {
        return QueueBuilder.durable(CREATE_QUEUE)
            .withArgument("x-dead-letter-exchange", "${EXCHANGE_NAME}.dlx")
            .withArgument("x-dead-letter-routing-key", "reservation.create.dlq")
            .build()
    }

    @Bean
    fun cancelQueue(): Queue {
        return QueueBuilder.durable(CANCEL_QUEUE)
            .withArgument("x-dead-letter-exchange", "${EXCHANGE_NAME}.dlx")
            .withArgument("x-dead-letter-routing-key", "reservation.cancel.dlq")
            .build()
    }

    @Bean
    fun paymentQueue(): Queue {
        return QueueBuilder.durable(PAYMENT_QUEUE)
            .withArgument("x-dead-letter-exchange", "${EXCHANGE_NAME}.dlx")
            .withArgument("x-dead-letter-routing-key", "reservation.payment.dlq")
            .build()
    }

    @Bean
    fun notificationQueue(): Queue {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build()
    }

    @Bean
    fun createBinding(): Binding {
        return BindingBuilder.bind(createQueue())
            .to(reservationExchange())
            .with(CREATE_ROUTING_KEY)
    }

    @Bean
    fun cancelBinding(): Binding {
        return BindingBuilder.bind(cancelQueue())
            .to(reservationExchange())
            .with(CANCEL_ROUTING_KEY)
    }

    @Bean
    fun paymentBinding(): Binding {
        return BindingBuilder.bind(paymentQueue())
            .to(reservationExchange())
            .with(PAYMENT_ROUTING_KEY)
    }

    @Bean
    fun notificationBinding(): Binding {
        return BindingBuilder.bind(notificationQueue())
            .to(reservationExchange())
            .with(NOTIFICATION_ROUTING_KEY)
    }

    @Bean
    fun messageConverter(): MessageConverter {
        return Jackson2JsonMessageConverter(objectMapper)
    }

    @Bean
    fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
        val template = RabbitTemplate(connectionFactory)
        template.messageConverter = messageConverter()
        return template
    }

    // Dead Letter Exchange 설정
    @Bean
    fun deadLetterExchange(): DirectExchange {
        return DirectExchange("${EXCHANGE_NAME}.dlx")
    }

    @Bean
    fun createDeadLetterQueue(): Queue {
        return QueueBuilder.durable("reservation.create.dlq").build()
    }

    @Bean
    fun cancelDeadLetterQueue(): Queue {
        return QueueBuilder.durable("reservation.cancel.dlq").build()
    }

    @Bean
    fun paymentDeadLetterQueue(): Queue {
        return QueueBuilder.durable("reservation.payment.dlq").build()
    }

    @Bean
    fun createDlqBinding(): Binding {
        return BindingBuilder.bind(createDeadLetterQueue())
            .to(deadLetterExchange())
            .with("reservation.create.dlq")
    }

    @Bean
    fun cancelDlqBinding(): Binding {
        return BindingBuilder.bind(cancelDeadLetterQueue())
            .to(deadLetterExchange())
            .with("reservation.cancel.dlq")
    }

    @Bean
    fun paymentDlqBinding(): Binding {
        return BindingBuilder.bind(paymentDeadLetterQueue())
            .to(deadLetterExchange())
            .with("reservation.payment.dlq")
    }
}
