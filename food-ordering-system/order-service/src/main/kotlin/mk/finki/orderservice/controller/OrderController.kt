package mk.finki.orderservice.controller

import mk.finki.orderservice.application.OrderApplicationService
import mk.finki.orderservice.domain.order.Order
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderApplicationService: OrderApplicationService
) {
    @PostMapping
    fun placeOrder(@RequestBody request: PlaceOrderRequest): Order {
        return orderApplicationService.placeOrder(
            request.customerId,
            request.restaurantId,
            request.items.map { 
                OrderApplicationService.ItemRequest(it.menuItemId, it.quantity, it.price, it.currency)
            }
        )
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: UUID): Order =
        orderApplicationService.getOrder(id).orElseThrow { RuntimeException("Order not found") }

    data class PlaceOrderRequest(
        val customerId: UUID,
        val restaurantId: UUID,
        val items: List<OrderItemRequest>
    )

    data class OrderItemRequest(
        val menuItemId: UUID,
        val quantity: Int,
        val price: java.math.BigDecimal,
        val currency: String
    )
}
