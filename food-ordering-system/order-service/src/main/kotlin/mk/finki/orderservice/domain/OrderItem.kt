package mk.finki.orderservice.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

/**
 * Entity inside the Order aggregate (not an aggregate root itself).
 * Price and name are SNAPSHOTS taken at order time — even if the restaurant
 * later changes the menu item price/name, this historical order stays accurate.
 * This is a deliberate DDD decision: Order does not hold a live reference to
 * MenuItem (different bounded context, different database).
 */
@Entity
@Table(name = "order_items")
class OrderItem(

    /** Reference only — the actual MenuItem lives in the Restaurant Service's own database. */
    @Column(name = "menu_item_id", nullable = false)
    var menuItemId: UUID,

    @Column(name = "item_name", nullable = false)
    var itemName: String,

    @Column(name = "unit_price", nullable = false)
    var unitPrice: BigDecimal,

    @Column(nullable = false)
    var quantity: Int
) {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    var order: Order? = null

    fun lineTotal(): BigDecimal = unitPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
}
