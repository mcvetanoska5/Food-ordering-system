package mk.finki.orderservice.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderEventProducer(private val kafkaTemplate: KafkaTemplate<String, Any>) {

    private val log = LoggerFactory.getLogger(OrderEventProducer::class.java)

    companion object {
        private const val ORDER_CREATED_TOPIC = "order.created"
        private const val ORDER_STATUS_CHANGED_TOPIC = "order.status.changed"
    }

    fun publishOrderCreated(event: OrderCreatedEvent) {
        log.info("Publishing OrderCreatedEvent for order {}", event.orderId)
        kafkaTemplate.send(ORDER_CREATED_TOPIC, event.orderId.toString(), event)
    }

    fun publishStatusChanged(event: OrderStatusChangedEvent) {
        log.info(
            "Publishing OrderStatusChangedEvent for order {} ({} -> {})",
            event.orderId, event.previousStatus, event.newStatus
        )
        kafkaTemplate.send(ORDER_STATUS_CHANGED_TOPIC, event.orderId.toString(), event)
    }
}
