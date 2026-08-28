package mk.finki.orderservice.application

import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.domain.order.OrderRepository
import mk.finki.orderservice.domain.order.events.OrderItemSnapshot
import mk.finki.orderservice.domain.order.events.OrderPlacedEvent
import mk.finki.orderservice.domain.order.valueobjects.*
import mk.finki.orderservice.handlers.EventMessagingEventHandler
import mk.finki.orderservice.services.impl.OrderAvailabilityService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class OrderApplicationService(
    private val orderRepository: OrderRepository,
    private val orderAvailabilityService: OrderAvailabilityService,
    private val eventMessagingEventHandler: EventMessagingEventHandler
) {
    @Transactional
    fun placeOrder(customerId: UUID, restaurantId: UUID, items: List<ItemRequest>): Order {
        val unavailableItems = items.filter { !orderAvailabilityService.isAvailable(it.menuItemId) }
        if (unavailableItems.isNotEmpty()) {
            throw RuntimeException("Some items are not available")
        }

        val order = Order(
            id = OrderId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            restaurantId = RestaurantId(restaurantId)
        )

        items.forEach {
            order.addItem(MenuItemId(it.menuItemId), it.quantity, Money(it.price, it.currency))
        }

        val savedOrder = orderRepository.save(order)
        val placedEvent = OrderPlacedEvent(
            orderId = savedOrder.id,
            customerId = savedOrder.customerId,
            restaurantId = savedOrder.restaurantId,
            items = savedOrder.items.map { OrderItemSnapshot(it.menuItemId.value, it.quantity) }
        )
        eventMessagingEventHandler.on(placedEvent)
        return savedOrder
    }

    @Transactional(readOnly = true)
    fun getOrder(id: UUID): Optional<Order> = orderRepository.findById(OrderId(id))

    data class ItemRequest(
        val menuItemId: UUID,
        val quantity: Int,
        val price: BigDecimal,
        val currency: String
    )
}
