package mk.finki.orderservice.mcp

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/orders")
class OrderMcpController(
    private val mcpServer: OrderMcpServer
) {

    data class PlaceOrderRequest(
        val customerId: UUID,
        val restaurantId: UUID,
        val address: String,
        val items: List<OrderAiTools.OrderItemDto>
    )

    // Note: "/place" and "/{orderId}/summary" are distinct paths from OrderController's
    // POST /api/orders and GET /api/orders/{id}, so there's no route collision.
    @PostMapping("/place", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun placeOrder(@RequestBody req: PlaceOrderRequest): ResponseEntity<String> {
        val body = mcpServer.placeOrder(req.customerId, req.restaurantId, req.address, req.items)
        return ResponseEntity.ok(body)
    }

    @GetMapping("/{orderId}/summary", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getOrderSummary(@PathVariable orderId: UUID): ResponseEntity<String> {
        val body = mcpServer.getOrderSummary(orderId)
        return ResponseEntity.ok(body)
    }
}