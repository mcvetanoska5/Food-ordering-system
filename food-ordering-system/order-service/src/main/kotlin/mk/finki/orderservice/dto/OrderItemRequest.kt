package mk.finki.orderservice.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.UUID

data class OrderItemRequest(
    @field:NotNull val menuItemId: UUID,
    @field:NotNull @field:Min(1) val quantity: Int
)
