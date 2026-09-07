package mk.finki.restaurantservice

import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import au.com.dius.pact.provider.junitsupport.Provider
import au.com.dius.pact.provider.junitsupport.State
import au.com.dius.pact.provider.junitsupport.loader.PactFolder
import mk.finki.restaurantservice.application.MenuItemApplicationService
import mk.finki.restaurantservice.controller.dto.MenuItemAvailabilityResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestTemplate
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.math.BigDecimal
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Provider("restaurant-service")
@PactFolder("../order-service/target/pacts")
class RestaurantProviderPactTest {

    @LocalServerPort
    private var port: Int = 0

    @MockBean
    private lateinit var menuItemApplicationService: MenuItemApplicationService

    @BeforeEach
    fun setup(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    fun pactVerificationTestTemplate(context: PactVerificationContext) {
        context.verifyInteraction()
    }

    @State("menu items exist")
    fun setupMenuItems() {
        val menuItemId = UUID.fromString("b3f1c2a0-2222-4a2b-9c3d-000000000002")
        `when`(menuItemApplicationService.checkAvailability(listOf(menuItemId)))
            .thenReturn(listOf(
                MenuItemAvailabilityResponse(
                    menuItemId = menuItemId,
                    available = true,
                    price = BigDecimal("350.00"),
                    currency = "MKD"
                )
            ))
    }
}