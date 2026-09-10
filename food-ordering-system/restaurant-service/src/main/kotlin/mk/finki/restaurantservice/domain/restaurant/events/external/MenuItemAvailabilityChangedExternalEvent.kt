package mk.finki.restaurantservice.domain.restaurant.events.external

import java.util.UUID

data class MenuItemAvailabilityChangedExternalEvent(
    val restaurantId: UUID,
    val menuItemId: UUID,
    val available: Boolean
)
