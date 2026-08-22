package mk.finki.orderservice.domain.order

import jakarta.persistence.*
import mk.finki.orderservice.domain.order.entities.OrderItem
import mk.finki.orderservice.domain.order.enums.OrderStatus
import mk.finki.orderservice.domain.order.exceptions.InvalidOrderStatusTransitionException
import mk.finki.orderservice.domain.order.valueobjects.*
import java.util.UUID

@Entity
@Table(name = "orders")
class Order(
    @EmbeddedId
    val id: OrderId,
    @Embedded
    val customerId: CustomerId,
    @Embedded
    val restaurantId: RestaurantId
) {
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.PLACED
        private set

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private val _items: MutableList<OrderItem> = mutableListOf()

    val items: List<OrderItem> get() = _items

    val totalPrice: Money
        get() = _items.map { it.subTotal }.reduceOrNull { acc, money -> acc.add(money) }
            ?: Money.zero()

    fun addItem(menuItemId: MenuItemId, quantity: Int, price: Money) {
        if (status != OrderStatus.PLACED) {
            throw IllegalStateException("Cannot add items to order in $status status")
        }
        val orderItem = OrderItem(UUID.randomUUID(), menuItemId, quantity, price)
        _items.add(orderItem)
    }

    fun confirm() {
        if (status != OrderStatus.PLACED) throw InvalidOrderStatusTransitionException(status, OrderStatus.CONFIRMED)
        status = OrderStatus.CONFIRMED
    }

    fun cancel() {
        if (status == OrderStatus.DELIVERED) throw InvalidOrderStatusTransitionException(status, OrderStatus.CANCELLED)
        status = OrderStatus.CANCELLED
    }

    // For JPA
    protected constructor() : this(OrderId(UUID.randomUUID()), CustomerId(UUID.randomUUID()), RestaurantId(UUID.randomUUID()))
}
