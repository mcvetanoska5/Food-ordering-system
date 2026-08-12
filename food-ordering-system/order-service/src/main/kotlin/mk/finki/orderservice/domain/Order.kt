package mk.finki.orderservice.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Aggregate root of the Order bounded context.
 * All modifications to OrderItems must go through this root (DDD invariant enforcement),
 * e.g. status transitions and total recalculation happen here, not on OrderItem directly.
 *
 * NOTE: kept as a plain class (not `data class`) on purpose — JPA entities with
 * mutable/lazy state shouldn't get generated equals()/hashCode()/toString().
 * The Kotlin "jpa" compiler plugin (configured in pom.xml) makes this class + its
 * properties open and adds the no-arg constructor Hibernate needs.
 */
@Entity
@Table(name = "orders")
class Order(

    @Column(name = "customer_id", nullable = false)
    var customerId: UUID,

    @Column(name = "restaurant_id", nullable = false)
    var restaurantId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OrderStatus = OrderStatus.CREATED,

    @Column(name = "delivery_address")
    var deliveryAddress: String? = null
) {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    var items: MutableList<OrderItem> = mutableListOf()

    @Column(name = "total_price", nullable = false)
    var totalPrice: BigDecimal = BigDecimal.ZERO

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @Column(name = "updated_at")
    var updatedAt: Instant? = null

    @PrePersist
    fun onCreate() {
        createdAt = Instant.now()
        updatedAt = Instant.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }

    fun addItem(item: OrderItem) {
        items.add(item)
        item.order = this
    }

    fun recalculateTotal() {
        totalPrice = items.fold(BigDecimal.ZERO) { acc, item -> acc + item.lineTotal() }
    }

    /** Enforces legal status transitions — an invariant of this aggregate. */
    fun transitionTo(next: OrderStatus) {
        val allowed = when (status) {
            OrderStatus.CREATED -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED
            OrderStatus.CONFIRMED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED
            OrderStatus.PREPARING -> next == OrderStatus.OUT_FOR_DELIVERY || next == OrderStatus.CANCELLED
            OrderStatus.OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED
            OrderStatus.DELIVERED, OrderStatus.CANCELLED -> false
        }
        if (!allowed) {
            throw IllegalStateException("Illegal order status transition: $status -> $next")
        }
        status = next
    }
}
