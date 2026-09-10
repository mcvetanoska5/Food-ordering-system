package mk.finki.orderservice.infrastructure.acl

import java.util.UUID

data class ValidatedMenuItem(
    val menuItemId: UUID,
    val available: Boolean
)

object MenuItemAntiCorruptionMapper {
    fun toInternal(dto: MenuItemAvailabilityDTO): ValidatedMenuItem =
        ValidatedMenuItem(dto.menuItemId, dto.available)
}
