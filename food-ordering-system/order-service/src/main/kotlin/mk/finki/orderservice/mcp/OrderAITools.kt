package mk.finki.orderservice.mcp

import mk.finki.orderservice.application.OrderApplicationService
import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.exception.OrderNotFoundException
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import java.util.*

/**
 * Tool wrapper that exposes OrderApplicationService as MCP tools.
 *
 * Methods are annotated with @Tool so Spring AI's MethodToolCallbackProvider can discover them.
 * These methods delegate to the existing application service to avoid duplicating business logic.
 */
@Component
class OrderAiTools(
    private val orderApplicationService: OrderApplicationService
) {

    @Tool(
        name = "placeOrder",
        description = "Place a new order for a customer at a restaurant. Parameters: customerId (UUID), " +
                "restaurantId (UUID), address (String), items (list of {menuItemId, quantity})"
    )
    fun placeOrder(
        @ToolParam(description = "UUID of the customer placing the order") customerId: UUID,
        @ToolParam(description = "UUID of the restaurant the order is placed at") restaurantId: UUID,
        @ToolParam(description = "Delivery address for the order") address: String,
        @ToolParam(description = "Items to order: each has menuItemId (UUID) and quantity (Int)") items: List<OrderItemDto>
    ): Map<String, Any?> {
        val order = orderApplicationService.placeOrder(
            customerId,
            restaurantId,
            address,
            items.map { OrderApplicationService.ItemRequest(it.menuItemId, it.quantity) }
        )
        return order.toSummaryMap()
    }

    @Tool(name = "getOrder", description = "Get an order by its UUID, including status, items and total price")
    fun getOrder(@ToolParam(description = "UUID of the order") orderId: UUID): Map<String, Any?> {
        val order = orderApplicationService.getOrder(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }
        return order.toSummaryMap()
    }

    private fun Order.toSummaryMap(): Map<String, Any?> = mapOf(
        "id" to id.value,
        "customerId" to customerId.value,
        "restaurantId" to restaurantId.value,
        "address" to address,
        "status" to status.name,
        "items" to items.map {
            mapOf(
                "id" to it.id,
                "menuItemId" to it.menuItemId.value,
                "quantity" to it.quantity,
                "price" to mapOf("amount" to it.price.amount, "currency" to it.price.currency),
                "subTotal" to mapOf("amount" to it.subTotal.amount, "currency" to it.subTotal.currency)
            )
        },
        "totalPrice" to mapOf("amount" to totalPrice.amount, "currency" to totalPrice.currency)
    )

    data class OrderItemDto(
        val menuItemId: UUID,
        val quantity: Int
    )
}