package mk.finki.restaurantservice.application

import mk.finki.restaurantservice.domain.restaurant.RestaurantRepository
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.domain.restaurant.events.MenuItemAvailabilityChangedEvent
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import mk.finki.restaurantservice.handlers.EventMessagingEventHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class MenuItemApplicationService(
    private val restaurantRepository: RestaurantRepository,
    private val eventMessagingEventHandler: EventMessagingEventHandler
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

    @Transactional
    fun setAvailability(restaurantId: UUID, menuItemId: UUID, available: Boolean) {
        val restaurant = restaurantRepository.findById(RestaurantId(restaurantId))
            .orElseThrow { IllegalArgumentException("Restaurant $restaurantId not found") }

        val menuItem = restaurant.menu.find { it.id == MenuItemId(menuItemId) }
            ?: throw IllegalArgumentException("Menu item $menuItemId not found")

        menuItem.updateStatus(if (available) MenuItemStatus.AVAILABLE else MenuItemStatus.OUT_OF_STOCK)

        val event = MenuItemAvailabilityChangedEvent(
            restaurantId = restaurant.id,
            menuItemId = menuItem.id,
            available = available
        )
        eventMessagingEventHandler.on(event)
    }
}
