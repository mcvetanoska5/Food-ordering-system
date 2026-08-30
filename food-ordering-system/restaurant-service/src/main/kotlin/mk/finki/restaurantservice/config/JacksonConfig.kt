package mk.finki.restaurantservice.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import mk.finki.restaurantservice.domain.restaurant.valueobjects.Identifier
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

@Configuration
class JacksonConfig {
    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
        builder.modules(
            SimpleModule().apply {
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
