package mk.finki.orderservice.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import mk.finki.orderservice.acl.external.MenuItemAvailabilityChangedEventDTO
import mk.finki.orderservice.acl.translator.EventTranslator
import mk.finki.orderservice.services.impl.OrderAvailabilityService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaEventConsumer(
    private val eventTranslator: EventTranslator,
    private val orderAvailabilityService: OrderAvailabilityService
) {
    private val logger = LoggerFactory.getLogger(KafkaEventConsumer::class.java)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @KafkaListener(topics = ["menu.item.availability.changed"], groupId = "order-service")
    fun listen(record: ConsumerRecord<String, String>) {
        try {
            val dto = objectMapper.readValue(record.value(), MenuItemAvailabilityChangedEventDTO::class.java)
            val command = eventTranslator.toUpdateMenuItemAvailabilityCommand(dto)
            orderAvailabilityService.handle(command)
        } catch (ex: Exception) {
            logger.error("Failed to process event: {}", record.value(), ex)
        }
    }
}
