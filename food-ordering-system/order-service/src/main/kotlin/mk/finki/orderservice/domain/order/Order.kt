package mk.finki.orderservice.domain.order

import io.swagger.v3.oas.annotations.media.Schema
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
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    val id: OrderId,
    @Embedded
    @Schema(example = "123e4567-e89b-12d3-a456-426614174001")
    val customerId: CustomerId,
    @Embedded
    @Schema(example = "123e4567-e89b-12d3-a456-426614174002")
    val restaurantId: RestaurantId,
    @Column(name = "address")
    @Schema(example = "123 Main St, New York, NY")
    val address: String
) {
    @Enumerated(EnumType.STRING)
    @Schema(example = "PLACED")
    var status: OrderStatus = OrderStatus.PLACED
        private set

    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private val _items: MutableList<OrderItem> = mutableListOf()

    @get:Schema(example = "[{\"id\":\"123e4567-e89b-12d3-a456-426614174003\",\"menuItemId\":\"123e4567-e89b-12d3-a456-426614174004\",\"quantity\":2,\"price\":{\"amount\":18.5,\"currency\":\"USD\"},\"subTotal\":{\"amount\":37.0,\"currency\":\"USD\"}}]")
    val items: List<OrderItem> get() = _items

    @get:Schema(example = "{\"amount\":37.00,\"currency\":\"USD\"}")
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
    protected constructor() : this(OrderId(UUID.randomUUID()), CustomerId(UUID.randomUUID()), RestaurantId(UUID.randomUUID()), "")
}
