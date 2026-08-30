package mk.finki.orderservice.infrastructure.acl

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class MenuItemAvailabilityDTO(
    val menuItemId: UUID,
    val available: Boolean,
    val price: BigDecimal? = null,
    val currency: String? = null
)
