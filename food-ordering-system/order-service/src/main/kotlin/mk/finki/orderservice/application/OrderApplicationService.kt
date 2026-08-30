package mk.finki.orderservice.application

import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.domain.order.OrderRepository
import mk.finki.orderservice.domain.order.events.OrderItemSnapshot
import mk.finki.orderservice.domain.order.events.OrderPlacedEvent
import mk.finki.orderservice.domain.order.valueobjects.*
import mk.finki.orderservice.handlers.EventMessagingEventHandler
import mk.finki.orderservice.infrastructure.client.RestaurantClient
import mk.finki.orderservice.services.impl.OrderAvailabilityService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Service
class OrderApplicationService(
    private val orderRepository: OrderRepository,
    private val orderAvailabilityService: OrderAvailabilityService,
    private val eventMessagingEventHandler: EventMessagingEventHandler,
    private val restaurantClient: RestaurantClient
) {
    private val idempotentOrders = ConcurrentHashMap<String, OrderId>()

    @Transactional
    fun placeOrder(customerId: UUID, restaurantId: UUID, address: String, items: List<ItemRequest>, idempotencyKey: String? = null): Order {
        if (idempotencyKey != null) {
            val existingOrderId = idempotentOrders[idempotencyKey]
            if (existingOrderId != null) {
                return orderRepository.findById(existingOrderId).orElseThrow {
                    idempotentOrders.remove(idempotencyKey)
                    RuntimeException("Order not found despite idempotency key")
                }
            }
        }

        val menuItemIds = items.map { it.menuItemId }
        val availability = restaurantClient.checkAvailability(menuItemIds)

        val order = Order(
            id = OrderId(UUID.randomUUID()),
            customerId = CustomerId(customerId),
            restaurantId = RestaurantId(restaurantId),
            address = address
        )

        items.forEach { itemRequest ->
            val av = availability.find { it.menuItemId == itemRequest.menuItemId }
                ?: throw MenuItemNotFoundException(itemRequest.menuItemId)

            if (!av.available) {
                throw MenuItemUnavailableException(itemRequest.menuItemId)
            }

            order.addItem(
                MenuItemId(itemRequest.menuItemId),
                itemRequest.quantity,
                Money(av.price ?: BigDecimal.ZERO, av.currency ?: "USD")
            )
        }

        val savedOrder = orderRepository.save(order)

        if (idempotencyKey != null) {
            idempotentOrders[idempotencyKey] = savedOrder.id
        }

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
        val quantity: Int
    )
}

class MenuItemNotFoundException(val menuItemId: UUID) : RuntimeException("Menu item $menuItemId not found")
class MenuItemUnavailableException(val menuItemId: UUID) : RuntimeException("Menu item $menuItemId is unavailable")
