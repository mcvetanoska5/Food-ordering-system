package mk.finki.restaurantservice.controller

import mk.finki.restaurantservice.application.MenuItemApplicationService
import mk.finki.restaurantservice.controller.dto.MenuItemAvailabilityResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/menu-items")
class MenuItemController(
    private val menuItemApplicationService: MenuItemApplicationService
) {
    @GetMapping("/availability")
    fun checkAvailability(@RequestParam ids: List<UUID>): List<MenuItemAvailabilityResponse> {
        val availability = menuItemApplicationService.checkAvailability(ids)
        return availability.map { MenuItemAvailabilityResponse(it.key, it.value) }
    }
}
