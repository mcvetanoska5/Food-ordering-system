package mk.finki.orderservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mk.finki.orderservice.application.OrderApplicationService
import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.exception.ErrorResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/orders")
@SecurityRequirement(name = "bearerAuth")
class OrderController(
    private val orderApplicationService: OrderApplicationService
) {
    @PostMapping
    @Operation(summary = "Place a new order")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Order created"),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest,
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: String?
    ): Order {
        return orderApplicationService.placeOrder(
            request.customerId,
            request.restaurantId,
            request.address,
            request.items.map { OrderApplicationService.ItemRequest(it.menuItemId, it.quantity) },
            idempotencyKey
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Order found"),
        ApiResponse(responseCode = "404", description = "Order not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    fun getOrder(@PathVariable id: UUID): Order =
        orderApplicationService.getOrder(id).orElseThrow { mk.finki.orderservice.exception.OrderNotFoundException(id) }

    data class PlaceOrderRequest(
        val customerId: UUID,
        val restaurantId: UUID,
        val address: String,
        val items: List<OrderItemRequest>
    )

    data class OrderItemRequest(
        val menuItemId: UUID,
        val quantity: Int
    )
}