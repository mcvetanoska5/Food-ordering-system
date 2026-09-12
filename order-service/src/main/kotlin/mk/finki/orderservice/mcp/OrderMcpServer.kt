package mk.finki.orderservice.mcp

import mk.finki.orderservice.application.OrderApplicationService
import mk.finki.orderservice.exception.OrderNotFoundException
import org.springframework.stereotype.Component
import java.util.*

/**
 * MCP Server implementation for Order Service.
 * Exposes tools (place order, read order) to AI clients via the Model Context Protocol.
 * Justification for embedding: avoids duplication of DTOs and gives direct access
 * to the existing Application Service layer.
 */
@Component
class OrderMcpServer(
    private val orderApplicationService: OrderApplicationService
) {
    // Mocking the MCP standard registration/loop for demonstration in docs

    fun placeOrder(customerId: UUID, restaurantId: UUID, address: String, items: List<OrderAiTools.OrderItemDto>): String {
        val order = orderApplicationService.placeOrder(
            customerId,
            restaurantId,
            address,
            items.map { OrderApplicationService.ItemRequest(it.menuItemId, it.quantity) }
        )
        return "Order ${order.id.value} placed for customer ${order.customerId.value} " +
                "- status: ${order.status} - total: ${order.totalPrice.amount} ${order.totalPrice.currency}"
    }

    fun getOrderSummary(orderId: UUID): String {
        val order = orderApplicationService.getOrder(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        val itemsSummary = order.items.joinToString("\n") {
            "  - ${it.menuItemId.value} x${it.quantity}: ${it.subTotal.amount} ${it.subTotal.currency}"
        }
        return "Order ${order.id.value} (${order.status})\n" +
                "Customer: ${order.customerId.value}\n" +
                "Restaurant: ${order.restaurantId.value}\n" +
                "Address: ${order.address}\n" +
                "Items:\n$itemsSummary\n" +
                "Total: ${order.totalPrice.amount} ${order.totalPrice.currency}"
    }
}