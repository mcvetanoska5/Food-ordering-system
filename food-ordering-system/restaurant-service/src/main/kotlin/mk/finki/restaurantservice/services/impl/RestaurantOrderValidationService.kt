package mk.finki.restaurantservice.services.impl

import mk.finki.restaurantservice.acl.translator.ValidateOrderItemsCommand
import mk.finki.restaurantservice.domain.restaurant.RestaurantRepository
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import org.springframework.stereotype.Service

@Service
class RestaurantOrderValidationService(
    private val restaurantRepository: RestaurantRepository
) {
    fun handle(command: ValidateOrderItemsCommand) {
        val restaurant = restaurantRepository.findById(command.restaurantId)
            .orElseThrow { IllegalArgumentException("Restaurant ${command.restaurantId.value} not found") }

        val unavailable = command.items.filter { item ->
            restaurant.menu.none { menuItem ->
                menuItem.id == item.menuItemId && menuItem.status == MenuItemStatus.AVAILABLE && !menuItem.deleted
            }
        }

        if (unavailable.isNotEmpty()) {
            throw IllegalStateException(
                "Order rejected because these menu items are not currently available: ${unavailable.map { it.menuItemId.value }}"
            )
        }
    }
}
