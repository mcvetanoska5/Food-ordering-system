package mk.finki.orderservice.infrastructure.client

import mk.finki.orderservice.infrastructure.acl.MenuItemAvailabilityDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@FeignClient(name = "restaurant-service")
interface RestaurantClient {
    @GetMapping("/api/menu-items/availability")
    fun checkAvailability(@RequestParam ids: List<UUID>): List<MenuItemAvailabilityDTO>
}
