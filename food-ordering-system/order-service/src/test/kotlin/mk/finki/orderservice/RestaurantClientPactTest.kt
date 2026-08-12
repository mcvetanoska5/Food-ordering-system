package mk.finki.orderservice

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import mk.finki.orderservice.client.RestaurantClient
import mk.finki.orderservice.dto.ValidateItemsRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID

/**
 * Consumer-driven contract test: Order Service (consumer) defines the contract it
 * expects from Restaurant & Catalog Service (provider) for POST /menu/items/validate.
 * Running this generates a pact file under target/pacts, which the Restaurant Service
 * team verifies against their real implementation (provider-side verification).
 *
 * Unlike a "does this JSON shape look right" test, this actually points a real
 * RestaurantClient (Feign) at the Pact mock server and asserts the DESERIALIZED
 * response — so it also catches mismatches between the contract and our own DTOs
 * (e.g. a renamed field that would silently deserialize to null).
 */
@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "restaurant-service")
class RestaurantClientPactTest {

    @Pact(consumer = "order-service")
    fun validateItemsPact(builder: PactDslWithProvider): RequestResponsePact =
        builder
            .given("menu items exist and are available")
            .uponReceiving("a request to validate cart items")
            .path("/menu/items/validate")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(
                """
                {
                  "restaurantId": "b3f1c2a0-1111-4a2b-9c3d-000000000001",
                  "items": [
                    { "menuItemId": "b3f1c2a0-2222-4a2b-9c3d-000000000002", "quantity": 2 }
                  ]
                }
                """.trimIndent()
            )
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(
                """
                {
                  "valid": true,
                  "validatedItems": [
                    {
                      "menuItemId": "b3f1c2a0-2222-4a2b-9c3d-000000000002",
                      "name": "Margherita Pizza",
                      "currentPrice": 350.00,
                      "available": true
                    }
                  ],
                  "unavailableItems": []
                }
                """.trimIndent()
            )
            .toPact()

    @Test
    @PactTestFor(pactMethod = "validateItemsPact")
    fun `validates items successfully`(mockServer: MockServer) {
        // Build a real Feign client (same encoder/decoder Spring Cloud OpenFeign
        // would wire up) pointed at the Pact mock server instead of a live service.
        val client = Feign.builder()
            .encoder(JacksonEncoder())
            .decoder(JacksonDecoder())
            .target(RestaurantClient::class.java, mockServer.getUrl())

        val response = client.validateItems(
            ValidateItemsRequest(
                restaurantId = UUID.fromString("b3f1c2a0-1111-4a2b-9c3d-000000000001"),
                items = listOf(
                    ValidateItemsRequest.Item(
                        UUID.fromString("b3f1c2a0-2222-4a2b-9c3d-000000000002"), 2
                    )
                )
            )
        )

        assertThat(response.valid).isTrue()
        assertThat(response.unavailableItems).isEmpty()
        assertThat(response.validatedItems).hasSize(1)
        assertThat(response.validatedItems[0].name).isEqualTo("Margherita Pizza")
        assertThat(response.validatedItems[0].currentPrice).isEqualByComparingTo(BigDecimal("350.00"))
        assertThat(response.validatedItems[0].available).isTrue()
    }
}
