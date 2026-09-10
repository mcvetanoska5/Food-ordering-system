package mk.finki.restaurantservice.mcp

import mk.finki.restaurantservice.application.MenuItemApplicationService
import mk.finki.restaurantservice.application.RestaurantApplicationService
import org.springframework.stereotype.Component
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import java.util.*

/**
 * Tool wrapper that exposes existing application services as MCP tools.
 *
 * Methods are annotated with @Tool so Spring AI's MethodToolCallbackProvider can discover them.
 * These methods delegate to existing application services to avoid duplicating business logic.
 */
@Component
class RestaurantAiTools(
    private val restaurantService: RestaurantApplicationService,
    private val menuItemService: MenuItemApplicationService
) {

    @Tool(name = "listRestaurants", description = "Return list of restaurants (id, name, address) as JSON array")
    fun listRestaurants(): List<Map<String, Any>> {
        val restaurants = restaurantService.listRestaurants()
        return restaurants.map { r -> mapOf("id" to r.id.value, "name" to r.name, "address" to r.address) }
    }

    @Tool(name = "getMenu", description = "Get the menu for a restaurant by UUID. Parameter: restaurantId (UUID)")
    fun getMenu(@ToolParam(description = "UUID of the restaurant") restaurantId: UUID): List<Map<String, Any>> {
        val restaurant = restaurantService.getRestaurant(restaurantId).orElseThrow { RuntimeException("Restaurant not found") }
        return restaurant.menu.map { m -> mapOf("id" to m.id.value, "name" to m.name, "price" to m.price.amount, "currency" to m.price.currency, "status" to m.status) }
    }

    @Tool(name = "checkAvailability", description = "Check availability for a list of menu item UUIDs. Parameter: ids (List<UUID>)")
    fun checkAvailability(@ToolParam(description = "List of menu item UUID strings") ids: List<UUID>): List<Map<String, Any>> {
        val availability = menuItemService.checkAvailability(ids)
        return availability.map { a -> mapOf("menuItemId" to a.menuItemId, "available" to a.available) }
    }
}
