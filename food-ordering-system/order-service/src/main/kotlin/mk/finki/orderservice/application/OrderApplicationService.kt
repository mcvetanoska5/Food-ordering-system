package mk.finki.orderservice.application

import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.domain.order.OrderRepository
import mk.finki.orderservice.domain.order.valueobjects.*
import mk.finki.orderservice.infrastructure.client.RestaurantClient
import mk.finki.orderservice.infrastructure.acl.MenuItemAntiCorruptionMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*

@Service
class OrderApplicationService(
    private val orderRepository: OrderRepository,
    private val restaurantClient: RestaurantClient
) {
    @Transactional
    fun placeOrder(customerId: UUID, restaurantId: UUID, items: List<ItemRequest>): Order {
        // ACL + Sync Call via Feign
        val availabilityDTOs = restaurantClient.checkAvailability(items.map { it.menuItemId })
        val validatedItems = availabilityDTOs.map { MenuItemAntiCorruptionMapper.toInternal(it) }
        
        if (validatedItems.any { !it.available }) {
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
        
        return orderRepository.save(order)
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
