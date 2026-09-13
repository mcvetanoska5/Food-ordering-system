package mk.finki.orderservice

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import mk.finki.orderservice.domain.order.events.OrderItemSnapshot
import mk.finki.orderservice.domain.order.events.OrderPlacedEvent
import mk.finki.orderservice.domain.order.valueobjects.CustomerId
import mk.finki.orderservice.domain.order.valueobjects.OrderId
import mk.finki.orderservice.domain.order.valueobjects.RestaurantId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import java.util.UUID

class OrderPlacedMessageProviderPactTest {

    private val mapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Test
    fun `order-service emits the current order placed message contract`() {
        val event = OrderPlacedEvent(
            orderId = OrderId(UUID.fromString("11111111-1111-4111-8111-111111111111")),
            customerId = CustomerId(UUID.fromString("aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa")),
            restaurantId = RestaurantId(UUID.fromString("22222222-2222-4222-8222-222222222222")),
            items = listOf(
                OrderItemSnapshot(
                    menuItemId = UUID.fromString("33333333-3333-4333-8333-333333333333"),
                    quantity = 2
                )
            )
        )

        val externalEvent = event.toExternalEvent() as mk.finki.orderservice.domain.order.events.external.OrderPlacedExternalEvent
        val expected = mapper.readTree(mapper.writeValueAsString(externalEvent))

        val pactPath = Paths.get("src/test/resources/pacts/restaurant-service-order-service.json")
        val pactJson = mapper.readTree(pactPath.toFile())
        val pactBody = mapper.readTree(pactJson["messages"].get(0)["contents"].asText())

        assertThat(pactBody).isEqualTo(expected)
    }
}
