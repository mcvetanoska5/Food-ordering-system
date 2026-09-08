package mk.finki.restaurantservice

import mk.finki.restaurantservice.application.MenuItemApplicationService
import mk.finki.restaurantservice.infrastructure.messaging.EventMessagingRepository
import mk.finki.restaurantservice.infrastructure.messaging.EventMessagingService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.util.UUID

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = [
        "spring.autoconfigure.exclude=org.axonframework.springboot.autoconfig.AxonAutoConfiguration,org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration,org.axonframework.springboot.autoconfig.AxonServerBusAutoConfiguration,org.axonframework.springboot.autoconfig.EventProcessingAutoConfiguration,org.axonframework.springboot.autoconfig.JpaAutoConfiguration,org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration,org.axonframework.springboot.autoconfig.JdbcAutoConfiguration,org.axonframework.springboot.autoconfig.TransactionAutoConfiguration,org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RestaurantProviderPactTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var menuItemApplicationService: MenuItemApplicationService

    @MockBean
    private lateinit var eventMessagingService: EventMessagingService

    @MockBean
    private lateinit var eventMessagingRepository: EventMessagingRepository

    @Test
    fun `restaurant provider matches the current availability contract`() {
        val menuItemId = UUID.fromString("b3f1c2a0-2222-4a2b-9c3d-000000000002")
        `when`(menuItemApplicationService.checkAvailability(listOf(menuItemId)))
            .thenReturn(
                listOf(
                    MenuItemApplicationService.MenuItemAvailabilityInfo(
                        menuItemId = menuItemId,
                        available = true,
                        price = BigDecimal("350.00"),
                        currency = "MKD"
                    )
                )
            )

        mockMvc.perform(
            get("/api/menu-items/availability")
                .param("ids", menuItemId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(jsonPath("$[0].menuItemId").value(menuItemId.toString()))
            .andExpect(jsonPath("$[0].available").value(true))
            .andExpect(jsonPath("$[0].price").value(350.00))
            .andExpect(jsonPath("$[0].currency").value("MKD"))
    }
}