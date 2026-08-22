package mk.finki.orderservice.domain.order.entities

import jakarta.persistence.*
import mk.finki.orderservice.domain.order.valueobjects.MenuItemId
import mk.finki.orderservice.domain.order.valueobjects.Money
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItem internal constructor(
    @Id
    val id: UUID,
    @Embedded
    val menuItemId: MenuItemId,
    val quantity: Int,
    @Embedded
    val price: Money
) {
    val subTotal: Money
        get() = price.multiply(quantity)
        
    // For JPA
    protected constructor() : this(UUID.randomUUID(), MenuItemId(UUID.randomUUID()), 0, Money.zero())
}
