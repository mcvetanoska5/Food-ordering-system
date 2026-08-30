package mk.finki.orderservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
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
    @Operation(summary = "Place a new order", description = "The order service resolves the authoritative menu item price before creating the order; the client only supplies menu item IDs and quantities.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Order created successfully (or existing order returned for duplicate idempotency key)",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Order::class), examples = [ExampleObject(value = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "customerId": "123e4567-e89b-12d3-a456-426614174001",
                  "restaurantId": "123e4567-e89b-12d3-a456-426614174002",
                  "address": "123 Main St, New York, NY",
                  "status": "PLACED",
                  "items": [
                    {
                      "id": "123e4567-e89b-12d3-a456-426614174003",
                      "menuItemId": "123e4567-e89b-12d3-a456-426614174004",
                      "quantity": 2,
                      "price": {
                        "amount": 18.50,
                        "currency": "USD"
                      },
                      "subTotal": {
                        "amount": 37.00,
                        "currency": "USD"
                      }
                    }
                  ],
                  "totalPrice": {
                    "amount": 37.00,
                    "currency": "USD"
                  }
                }
                """))])
        ),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Menu item not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "409", description = "Menu item unavailable", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = PlaceOrderRequest::class),
            examples = [ExampleObject(value = """
                {
                  "customerId": "123e4567-e89b-12d3-a456-426614174001",
                  "restaurantId": "123e4567-e89b-12d3-a456-426614174002",
                  "address": "123 Main St, New York, NY",
                  "items": [
                    { "menuItemId": "123e4567-e89b-12d3-a456-426614174004", "quantity": 2 }
                  ]
                }
                """)]
        )]
    )
    fun placeOrder(
        @RequestBody request: PlaceOrderRequest,
        @Parameter(description = "Optional idempotency key")
        @RequestHeader(value = "Idempotency-Key", required = false) idempotencyKey: String?
    ): Order {
        return orderApplicationService.placeOrder(
            request.customerId,
            request.restaurantId,
            request.address,
            request.items.map {
                OrderApplicationService.ItemRequest(it.menuItemId, it.quantity)
            },
            idempotencyKey
        )
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Order status is updated asynchronously via Kafka/Saga. This endpoint only reflects the current persisted state and it is read-only via REST.")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Order found",
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Order::class), examples = [ExampleObject(value = """
                {
                  "id": "123e4567-e89b-12d3-a456-426614174000",
                  "customerId": "123e4567-e89b-12d3-a456-426614174001",
                  "restaurantId": "123e4567-e89b-12d3-a456-426614174002",
                  "address": "123 Main St, New York, NY",
                  "status": "PLACED",
                  "items": [],
                  "totalPrice": { "amount": 0, "currency": "USD" }
                }
                """))])
        ),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Order not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    fun getOrder(@PathVariable id: UUID): Order =
        orderApplicationService.getOrder(id).orElseThrow { mk.finki.orderservice.exception.OrderNotFoundException(id) }

    data class PlaceOrderRequest(
        @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
        val customerId: UUID,
        @Schema(example = "123e4567-e89b-12d3-a456-426614174001")
        val restaurantId: UUID,
        @Schema(example = "123 Main St, New York, NY")
        val address: String,
        val items: List<OrderItemRequest>
    )

    data class OrderItemRequest(
        @Schema(example = "123e4567-e89b-12d3-a456-426614174002")
        val menuItemId: UUID,
        @Schema(example = "2")
        val quantity: Int
    )
}
