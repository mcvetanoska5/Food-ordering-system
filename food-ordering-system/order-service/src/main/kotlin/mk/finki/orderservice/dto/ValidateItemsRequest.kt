package mk.finki.orderservice.dto

import java.util.UUID

/**
 * Request body sent to Restaurant & Catalog Service's POST /menu/items/validate.
 * This is the AGREED CONTRACT between the two services — keep in sync with the
 * colleague's Restaurant Service DTOs and the Pact contract test.
 */
data class ValidateItemsRequest(
    val restaurantId: UUID,
    val items: List<Item>
) {
    data class Item(val menuItemId: UUID, val quantity: Int)
}
