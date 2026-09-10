package mk.finki.restaurantservice.mcp

import mk.finki.restaurantservice.application.MenuItemApplicationService
import mk.finki.restaurantservice.application.RestaurantApplicationService
import org.springframework.stereotype.Component
import java.util.*

/**
 * MCP Server implementation for Restaurant Service.
 * Exposes read-only tools to AI clients via the Model Context Protocol.
 * Justification for embedding: Avoids duplication of DTOs and provides direct access to Application Services.
 */
@Component
class RestaurantMcpServer(
    private val restaurantService: RestaurantApplicationService,
    private val menuItemService: MenuItemApplicationService
) {
    // Mocking the MCP standard registration/loop for demonstration in docs
    
    fun listRestaurants(): String {
        val restaurants = restaurantService.listRestaurants()
        return restaurants.joinToString("\n") { "${it.id.value}: ${it.name} at ${it.address}" }
    }

    fun getRestaurantMenu(restaurantId: UUID): String {
        val restaurant = restaurantService.getRestaurant(restaurantId)
            .orElseThrow { RuntimeException("Restaurant not found") }
        return restaurant.menu.joinToString("\n") { "- ${it.name}: ${it.price.amount} ${it.price.currency} (${it.status})" }
    }

    fun checkItemAvailability(ids: List<UUID>): String {
        val availability = menuItemService.checkAvailability(ids)
        return availability.joinToString("\n") { "${it.menuItemId}: ${if (it.available) "Available" else "Unavailable"}" }
    }
}
