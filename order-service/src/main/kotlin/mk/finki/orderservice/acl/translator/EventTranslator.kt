package mk.finki.orderservice.acl.translator

import mk.finki.orderservice.acl.external.MenuItemAvailabilityChangedEventDTO
import mk.finki.orderservice.domain.order.valueobjects.MenuItemId
import org.springframework.stereotype.Service

@Service
class EventTranslator {
    fun toUpdateMenuItemAvailabilityCommand(dto: MenuItemAvailabilityChangedEventDTO): UpdateMenuItemAvailabilityCommand =
        UpdateMenuItemAvailabilityCommand(
            menuItemId = MenuItemId(dto.menuItemId),
            available = dto.available
        )
}

data class UpdateMenuItemAvailabilityCommand(
    val menuItemId: MenuItemId,
    val available: Boolean
)
