package mk.finki.restaurantservice.controller.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal
import java.util.*

data class MenuItemAvailabilityResponse(
    @Schema(example = "123e4567-e89b-12d3-a456-426614174002")
    val menuItemId: UUID,
    @Schema(example = "true")
    val available: Boolean,
    @Schema(example = "12.99")
    val price: BigDecimal? = null,
    @Schema(example = "USD")
    val currency: String? = null
)
