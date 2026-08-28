package mk.finki.orderservice.acl.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuItemAvailabilityChangedEventDTO(
    val restaurantId: UUID,
    val menuItemId: UUID,
    val available: Boolean
)
