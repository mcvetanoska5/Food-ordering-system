package mk.finki.orderservice.controller

import jakarta.validation.Valid
import mk.finki.orderservice.domain.OrderStatus
import mk.finki.orderservice.dto.CreateOrderRequest
import mk.finki.orderservice.dto.OrderResponse
import mk.finki.orderservice.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun createOrder(@Valid @RequestBody request: CreateOrderRequest): ResponseEntity<OrderResponse> {
        val response = orderService.createOrder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    fun getOrder(@PathVariable id: UUID): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(orderService.getOrder(id))

    @GetMapping("/customer/{customerId}")
    fun getOrdersByCustomer(@PathVariable customerId: UUID): ResponseEntity<List<OrderResponse>> =
        ResponseEntity.ok(orderService.getOrdersByCustomer(customerId))

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @RequestParam status: OrderStatus
    ): ResponseEntity<OrderResponse> =
        ResponseEntity.ok(orderService.updateStatus(id, status))
}
