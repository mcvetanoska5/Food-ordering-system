package mk.finki.restaurantservice.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import mk.finki.restaurantservice.acl.external.OrderPlacedEventDTO
import mk.finki.restaurantservice.acl.translator.OrderPlacedEventTranslator
import mk.finki.restaurantservice.services.impl.RestaurantOrderValidationService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class KafkaOrderPlacedEventConsumer(
    private val eventTranslator: OrderPlacedEventTranslator,
    private val restaurantOrderValidationService: RestaurantOrderValidationService
) {
    private val logger = LoggerFactory.getLogger(KafkaOrderPlacedEventConsumer::class.java)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @KafkaListener(topics = ["order.placed"], groupId = "restaurant-service")
    fun listen(record: ConsumerRecord<String, String>) {
        try {
            val dto = objectMapper.readValue(record.value(), OrderPlacedEventDTO::class.java)
            val command = eventTranslator.toValidateOrderItemsCommand(dto)
            restaurantOrderValidationService.handle(command)
        } catch (ex: Exception) {
            logger.error("Failed to process event: {}", record.value(), ex)
        }
    }
}
