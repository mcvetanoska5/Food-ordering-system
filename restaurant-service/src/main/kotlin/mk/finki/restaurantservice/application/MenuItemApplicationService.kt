package mk.finki.restaurantservice.application

import mk.finki.restaurantservice.domain.restaurant.RestaurantRepository
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.domain.restaurant.events.MenuItemAvailabilityChangedEvent
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import mk.finki.restaurantservice.handlers.EventMessagingEventHandler
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class MenuItemApplicationService(
    private val restaurantRepository: RestaurantRepository,
    private val eventMessagingEventHandler: EventMessagingEventHandler
) {
    @Transactional(readOnly = true)
    fun checkAvailability(menuItemIds: List<UUID>): List<MenuItemAvailabilityInfo> {
        val restaurants = restaurantRepository.findAll()
        val results = mutableListOf<MenuItemAvailabilityInfo>()

        menuItemIds.forEach { id ->
            val item = restaurants.flatMap { it.menu }.find { it.id.value == id }
            val info = if (item != null) {
                MenuItemAvailabilityInfo(
                    id,
                    item.status == MenuItemStatus.AVAILABLE && !item.deleted,
                    item.price.amount,
                    item.price.currency
                )
            } else {
                MenuItemAvailabilityInfo(id, false, null, null)
            }
            results.add(info)
        }
        return results
    }

    data class MenuItemAvailabilityInfo(
        val menuItemId: UUID,
        val available: Boolean,
        val price: BigDecimal?,
        val currency: String?
    )

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
