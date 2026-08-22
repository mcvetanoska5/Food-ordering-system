package mk.finki.restaurantservice.domain.restaurant.entities

import jakarta.persistence.*
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.Money
import java.util.UUID

@Entity
@Table(name = "menu_items")
class MenuItem internal constructor(
    @EmbeddedId
    val id: MenuItemId,
    var name: String,
    @Embedded
    var price: Money,
    @Enumerated(EnumType.STRING)
    var status: MenuItemStatus
) {
    @Column(name = "deleted")
    var deleted: Boolean = false
        private set

    fun discontinue() {
        this.deleted = true
        this.status = MenuItemStatus.DISCONTINUED
    }

    fun updatePrice(newPrice: Money) {
        this.price = newPrice
    }

    fun updateStatus(newStatus: MenuItemStatus) {
        if (!deleted) {
            this.status = newStatus
        }
    }
    
    // For JPA
    protected constructor() : this(MenuItemId(UUID.randomUUID()), "", Money.zero(), MenuItemStatus.AVAILABLE)
}
