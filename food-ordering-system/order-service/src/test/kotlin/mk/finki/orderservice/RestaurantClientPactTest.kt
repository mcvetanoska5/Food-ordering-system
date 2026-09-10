package mk.finki.orderservice

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import mk.finki.orderservice.infrastructure.client.RestaurantClient
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "restaurant-service")
class RestaurantClientPactTest {

    @Pact(consumer = "order-service")
    fun checkAvailabilityPact(builder: PactBuilder): V4Pact =
        builder
            .given("menu items exist")
            .expectsToReceiveHttpInteraction("a request for menu item availability") { interaction ->
                interaction
                    .withRequest { request ->
                        request
                            .method("GET")
                            .path("/api/menu-items/availability")
                            .queryParameters("ids=b3f1c2a0-2222-4a2b-9c3d-000000000002")
                    }
                    .willRespondWith { response ->
                        response
                            .status(200)
                            .headers(mapOf("Content-Type" to "application/json"))
                            .body(
                                """
                                [
                                  {
                                    "menuItemId": "b3f1c2a0-2222-4a2b-9c3d-000000000002",
                                    "available": true,
                                    "price": 350.00,
                                    "currency": "MKD"
                                  }
                                ]
                                """.trimIndent()
                            )
                    }
            }
            .toPact()

    @Test
    @PactTestFor(pactMethod = "checkAvailabilityPact")
    fun `checks availability successfully`(mockServer: MockServer) {
        val mapper = jacksonObjectMapper()
        val client = Feign.builder()
            .contract(SpringMvcContract())
            .encoder(JacksonEncoder(mapper))
            .decoder(JacksonDecoder(mapper))
            .target(RestaurantClient::class.java, mockServer.getUrl())

        val response = client.checkAvailability(
            listOf(UUID.fromString("b3f1c2a0-2222-4a2b-9c3d-000000000002"))
        )

        assertThat(response).hasSize(1)
        assertThat(response[0].menuItemId).isEqualTo(UUID.fromString("b3f1c2a0-2222-4a2b-9c3d-000000000002"))
        assertThat(response[0].available).isTrue()
        assertThat(response[0].price).isEqualByComparingTo(BigDecimal("350.00"))
        assertThat(response[0].currency).isEqualTo("MKD")
    }
}
