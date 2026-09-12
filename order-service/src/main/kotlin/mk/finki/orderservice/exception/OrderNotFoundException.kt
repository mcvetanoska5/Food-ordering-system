package mk.finki.orderservice.exception

import java.util.UUID

class OrderNotFoundException(id: UUID) : RuntimeException("Order not found: $id")
