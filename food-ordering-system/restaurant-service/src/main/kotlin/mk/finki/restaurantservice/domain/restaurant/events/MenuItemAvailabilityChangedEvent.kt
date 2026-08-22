package mk.finki.restaurantservice.domain.restaurant.events

import mk.finki.restaurantservice.domain.restaurant.events.external.MenuItemAvailabilityChangedExternalEvent
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId

data class MenuItemAvailabilityChangedEvent(
    override val aggregateId: RestaurantId,
    val menuItemId: MenuItemId,
    val available: Boolean
) : RestaurantEvent(aggregateId) {
    override fun toExternalEvent(): MenuItemAvailabilityChangedExternalEvent =
        MenuItemAvailabilityChangedExternalEvent(aggregateId.value, menuItemId.value, available)
}
