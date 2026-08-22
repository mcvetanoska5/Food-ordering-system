package mk.finki.restaurantservice.controller.dto

import java.math.BigDecimal
import java.util.*

data class MenuItemAvailabilityResponse(
    val menuItemId: UUID,
    val available: Boolean
)
