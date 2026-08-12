package mk.finki.orderservice.client

import mk.finki.orderservice.dto.ValidateItemsRequest
import mk.finki.orderservice.dto.ValidateItemsResponse
import org.springframework.stereotype.Component

/**
 * Fallback used when Restaurant Service is unreachable/timing out.
 * We fail closed (valid = false) — never place an order we couldn't verify.
 */
@Component
class RestaurantClientFallback : RestaurantClient {

    override fun validateItems(request: ValidateItemsRequest): ValidateItemsResponse {
        val allUnavailable = request.items.map { it.menuItemId }
        return ValidateItemsResponse(
            valid = false,
            validatedItems = emptyList(),
            unavailableItems = allUnavailable
        )
    }
}
