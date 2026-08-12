package mk.finki.orderservice.dto

import java.math.BigDecimal
import java.util.UUID

/**
 * Response returned by Restaurant & Catalog Service's POST /menu/items/validate.
 * "valid" is true only if ALL requested items exist and are currently available.
 * unavailableItems lists menuItemIds that failed the check (out of stock / not found).
 */
data class ValidateItemsResponse(
    val valid: Boolean,
    val validatedItems: List<MenuItemValidation>,
    val unavailableItems: List<UUID>
) {
    data class MenuItemValidation(
        val menuItemId: UUID,
        val name: String,
        val currentPrice: BigDecimal,
        val available: Boolean
    )
}
