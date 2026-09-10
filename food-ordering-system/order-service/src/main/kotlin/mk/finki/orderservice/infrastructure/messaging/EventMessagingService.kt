package mk.finki.orderservice.infrastructure.messaging

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

interface EventMessagingService {
    fun send(topic: String, key: String, payload: String)
}

interface EventMessagingRepository {
    fun send(topic: String, key: String, payload: String)
}

@Service
class EventMessagingServiceImpl(
    private val eventMessagingRepository: EventMessagingRepository
) : EventMessagingService {
    override fun send(topic: String, key: String, payload: String) =
        eventMessagingRepository.send(topic, key, payload)
}

@Repository
class KafkaMessagingRepositoryImpl(
    private val kafkaTemplate: KafkaTemplate<String, String>
) : EventMessagingRepository {
    private val logger = LoggerFactory.getLogger(KafkaMessagingRepositoryImpl::class.java)

    override fun send(topic: String, key: String, payload: String) {
        kafkaTemplate.send(topic, key, payload).whenComplete { result, ex ->
            if (ex != null) {
                logger.error("Failed to publish [{}] to Kafka: {}", key, ex.message, ex)
            } else {
                logger.info(
                    "Published [{}] to topic {} partition {} offset {}",
                    key,
                    result.recordMetadata.topic(),
                    result.recordMetadata.partition(),
                    result.recordMetadata.offset()
                )
            }
        }
    }
}
