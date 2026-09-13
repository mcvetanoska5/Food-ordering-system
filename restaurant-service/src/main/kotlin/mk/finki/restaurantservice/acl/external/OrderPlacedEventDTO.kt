package mk.finki.restaurantservice.acl.external

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderPlacedEventDTO(
    val orderId: String,
    val restaurantId: String,
    val items: List<OrderLineItemDTO>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrderLineItemDTO(
    val menuItemId: String,
    val quantity: Int
)
