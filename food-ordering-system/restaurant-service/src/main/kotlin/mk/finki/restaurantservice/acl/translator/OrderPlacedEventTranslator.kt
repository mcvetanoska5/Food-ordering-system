package mk.finki.restaurantservice.acl.translator

import mk.finki.restaurantservice.acl.external.OrderPlacedEventDTO
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderPlacedEventTranslator {
    fun toValidateOrderItemsCommand(dto: OrderPlacedEventDTO): ValidateOrderItemsCommand =
        ValidateOrderItemsCommand(
            restaurantId = RestaurantId(UUID.fromString(dto.restaurantId)),
            items = dto.items.map {
                OrderLineItemCommand(
                    menuItemId = MenuItemId(UUID.fromString(it.menuItemId)),
                    quantity = it.quantity
                )
            }
        )
}

data class ValidateOrderItemsCommand(
    val restaurantId: RestaurantId,
    val items: List<OrderLineItemCommand>
)

data class OrderLineItemCommand(
    val menuItemId: MenuItemId,
    val quantity: Int
)
