package mk.finki.orderservice.domain

/**
 * Lifecycle states of an Order.
 * Transitions are enforced in Order.transitionTo() (no illegal jumps, e.g. CREATED -> DELIVERED).
 */
enum class OrderStatus {
    CREATED,
    CONFIRMED,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
