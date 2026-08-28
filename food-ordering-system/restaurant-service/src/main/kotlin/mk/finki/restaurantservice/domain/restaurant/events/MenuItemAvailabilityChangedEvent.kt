package mk.finki.restaurantservice.domain.restaurant.events

import mk.finki.restaurantservice.domain.restaurant.events.external.MenuItemAvailabilityChangedExternalEvent
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId

data class MenuItemAvailabilityChangedEvent(
    override val restaurantId: RestaurantId,
    val menuItemId: MenuItemId,
    val available: Boolean
) : RestaurantEvent(restaurantId) {
    override fun toExternalEvent(): MenuItemAvailabilityChangedExternalEvent =
        MenuItemAvailabilityChangedExternalEvent(restaurantId = restaurantId.value, menuItemId = menuItemId.value, available = available)
}
