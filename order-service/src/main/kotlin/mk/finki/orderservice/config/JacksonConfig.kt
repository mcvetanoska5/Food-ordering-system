package mk.finki.orderservice.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import mk.finki.orderservice.domain.order.valueobjects.CustomerId
import mk.finki.orderservice.domain.order.valueobjects.Identifier
import mk.finki.orderservice.domain.order.valueobjects.MenuItemId
import mk.finki.orderservice.domain.order.valueobjects.OrderId
import mk.finki.orderservice.domain.order.valueobjects.RestaurantId
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class JacksonConfig {
    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
        builder.modulesToInstall(
            SimpleModule().apply {
                addSerializer(OrderId::class.java, UuidStringSerializer { it.value })
                addDeserializer(OrderId::class.java, UuidStringDeserializer { OrderId(it) })

                addSerializer(CustomerId::class.java, UuidStringSerializer { it.value })
                addDeserializer(CustomerId::class.java, UuidStringDeserializer { CustomerId(it) })

                addSerializer(MenuItemId::class.java, UuidStringSerializer { it.value })
                addDeserializer(MenuItemId::class.java, UuidStringDeserializer { MenuItemId(it) })

                addSerializer(RestaurantId::class.java, UuidStringSerializer { it.value })
                addDeserializer(RestaurantId::class.java, UuidStringDeserializer { RestaurantId(it) })
            }
        )
    }
}

class UuidStringSerializer<T : Identifier<UUID>>(private val extractor: (T) -> UUID) : JsonSerializer<T>() {
    override fun serialize(value: T?, gen: JsonGenerator, serializers: SerializerProvider) {
        if (value == null) {
            gen.writeNull()
            return
        }
        gen.writeString(extractor(value).toString())
    }
}

class UuidStringDeserializer<T : Identifier<UUID>>(private val factory: (UUID) -> T) : JsonDeserializer<T>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): T {
        return factory(UUID.fromString(parser.text))
    }
}