package mk.finki.restaurantservice.controller

import mk.finki.restaurantservice.application.RestaurantApplicationService
import mk.finki.restaurantservice.domain.restaurant.Restaurant
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/api/restaurants")
class RestaurantController(
    private val restaurantService: RestaurantApplicationService
) {
    @GetMapping
    fun getAll(): List<Restaurant> = restaurantService.listRestaurants()

    @PostMapping("/{id}/menu-items")
    fun addMenuItem(
        @PathVariable id: UUID,
        @RequestBody request: CreateMenuItemRequest
    ) {
        restaurantService.addMenuItem(id, request.name, request.price, request.currency)
    }

    data class CreateMenuItemRequest(val name: String, val price: BigDecimal, val currency: String = "USD")
}
