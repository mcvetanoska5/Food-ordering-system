package mk.finki.orderservice.service

import mk.finki.orderservice.client.RestaurantClient
import mk.finki.orderservice.domain.Order
import mk.finki.orderservice.domain.OrderItem
import mk.finki.orderservice.domain.OrderStatus
import mk.finki.orderservice.dto.CreateOrderRequest
import mk.finki.orderservice.dto.OrderResponse
import mk.finki.orderservice.dto.ValidateItemsRequest
import mk.finki.orderservice.exception.InvalidOrderException
import mk.finki.orderservice.exception.OrderNotFoundException
import mk.finki.orderservice.messaging.OrderCreatedEvent
import mk.finki.orderservice.messaging.OrderEventProducer
import mk.finki.orderservice.messaging.OrderStatusChangedEvent
import mk.finki.orderservice.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val restaurantClient: RestaurantClient,
    private val eventProducer: OrderEventProducer
) : OrderService {

    @Transactional
    override fun createOrder(request: CreateOrderRequest): OrderResponse {

        // 1. Synchronous validation against Restaurant & Catalog Service (Feign/REST).
        //    This is the key cross-service call: we never trust cart data blindly —
        //    items must exist, be available, and we take the CURRENT price from the source of truth.
        val validateRequest = ValidateItemsRequest(
            restaurantId = request.restaurantId,
            items = request.items.map { ValidateItemsRequest.Item(it.menuItemId, it.quantity) }
        )

        val validation = restaurantClient.validateItems(validateRequest)

        if (!validation.valid) {
            throw InvalidOrderException(validation.unavailableItems)
        }

        val byId = validation.validatedItems.associateBy { it.menuItemId }
        val requestedQuantities = request.items.associate { it.menuItemId to it.quantity }

        // 2. Build the Order aggregate. Prices are SNAPSHOTTED from the validation
        //    response now — they will not change even if the restaurant edits its menu later.
        val order = Order(
            customerId = request.customerId,
            restaurantId = request.restaurantId,
            status = OrderStatus.CREATED,
            deliveryAddress = request.deliveryAddress
        )

        requestedQuantities.forEach { (menuItemId, qty) ->
            val v = byId.getValue(menuItemId)
            order.addItem(
                OrderItem(
                    menuItemId = menuItemId,
                    itemName = v.name,
                    unitPrice = v.currentPrice,
                    quantity = qty
                )
            )
        }

        order.recalculateTotal()
        val saved = orderRepository.save(order)

        // 3. Publish asynchronous event — Notification/Payment services react independently.
        eventProducer.publishOrderCreated(
            OrderCreatedEvent(
                saved.id, saved.customerId, saved.restaurantId, saved.totalPrice, Instant.now()
            )
        )

        return OrderResponse.from(saved)
    }

    override fun getOrder(id: UUID): OrderResponse = OrderResponse.from(findOrThrow(id))

    override fun getOrdersByCustomer(customerId: UUID): List<OrderResponse> =
        orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).map { OrderResponse.from(it) }

    @Transactional
    override fun updateStatus(id: UUID, newStatus: OrderStatus): OrderResponse {
        val order = findOrThrow(id)
        val previous = order.status

        order.transitionTo(newStatus) // throws IllegalStateException if not a legal transition
        val saved = orderRepository.save(order)

        eventProducer.publishStatusChanged(
            OrderStatusChangedEvent(saved.id, previous, saved.status, Instant.now())
        )

        return OrderResponse.from(saved)
    }

    private fun findOrThrow(id: UUID): Order =
        orderRepository.findById(id).orElseThrow { OrderNotFoundException(id) }
}
