package mk.finki.orderservice.domain.order.entities

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import mk.finki.orderservice.domain.order.valueobjects.MenuItemId
import mk.finki.orderservice.domain.order.valueobjects.Money
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItem internal constructor(
    @Id
    @Schema(example = "123e4567-e89b-12d3-a456-426614174003")
    val id: UUID,
    @Embedded
    @Schema(example = "123e4567-e89b-12d3-a456-426614174004")
    val menuItemId: MenuItemId,
    @Schema(example = "2")
    val quantity: Int,
    @Embedded
    val price: Money
) {
    @get:Schema(example = "{\"amount\":37.00,\"currency\":\"USD\"}")
    val subTotal: Money
        get() = price.multiply(quantity)
        
    // For JPA
    protected constructor() : this(UUID.randomUUID(), MenuItemId(UUID.randomUUID()), 0, Money.zero())
}
