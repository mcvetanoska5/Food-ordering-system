package mk.finki.restaurantservice.application

import mk.finki.restaurantservice.domain.restaurant.RestaurantRepository
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MenuItemApplicationService(
    private val restaurantRepository: RestaurantRepository
) {
    @Transactional(readOnly = true)
    fun checkAvailability(menuItemIds: List<UUID>): Map<UUID, Boolean> {
        val restaurants = restaurantRepository.findAll()
        val results = mutableMapOf<UUID, Boolean>()
        
        menuItemIds.forEach { id ->
            val item = restaurants.flatMap { it.menu }.find { it.id.value == id }
            results[id] = item?.let { it.status == MenuItemStatus.AVAILABLE && !it.deleted } ?: false
        }
        return results
    }
}
