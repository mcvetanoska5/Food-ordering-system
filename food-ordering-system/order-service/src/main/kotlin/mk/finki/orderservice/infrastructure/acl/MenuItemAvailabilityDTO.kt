package mk.finki.orderservice.infrastructure.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuItemAvailabilityDTO(
    val menuItemId: UUID,
    val available: Boolean
)
