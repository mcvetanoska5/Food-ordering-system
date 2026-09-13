package mk.finki.restaurantservice

import au.com.dius.pact.consumer.MessagePactBuilder
import au.com.dius.pact.core.model.PactSpecVersion
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mk.finki.restaurantservice.acl.external.OrderPlacedEventDTO
import mk.finki.restaurantservice.acl.translator.OrderPlacedEventTranslator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class OrderPlacedMessagePactTest {

    @Test
    fun `restaurant-service consumes the current order placed message contract`() {
        val payload = """
            {
              "orderId": "11111111-1111-4111-8111-111111111111",
              "restaurantId": "22222222-2222-4222-8222-222222222222",
              "items": [
                { "menuItemId": "33333333-3333-4333-8333-333333333333", "quantity": 2 }
              ]
            }
        """.trimIndent()

        val pact = MessagePactBuilder()
            .consumer("restaurant-service")
            .hasPactWith("order-service")
            .given("order placed")
            .expectsToReceive("an order placed event")
            .withContent(payload)
            .toPact<au.com.dius.pact.core.model.messaging.MessagePact>()

        pact.write("target/pacts", PactSpecVersion.V3)
        val pactDir = java.io.File("target/pacts")
        assertThat(pactDir.exists()).isTrue()
        assertThat(pactDir.listFiles()?.any { it.name.endsWith(".json") }).isTrue()

        val mapper = jacksonObjectMapper()
        val dto = mapper.readValue(payload, OrderPlacedEventDTO::class.java)
        val command = OrderPlacedEventTranslator().toValidateOrderItemsCommand(dto)

        assertThat(command.restaurantId.value).isEqualTo(UUID.fromString("22222222-2222-4222-8222-222222222222"))
        assertThat(command.items).hasSize(1)
        assertThat(command.items[0].menuItemId.value).isEqualTo(UUID.fromString("33333333-3333-4333-8333-333333333333"))
        assertThat(command.items[0].quantity).isEqualTo(2)
    }
}
