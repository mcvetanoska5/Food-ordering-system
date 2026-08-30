package mk.finki.restaurantservice.domain.restaurant.entities

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.Money
import java.util.UUID

@Entity
@Table(name = "menu_items")
class MenuItem internal constructor(
    @EmbeddedId
    @Schema(example = "123e4567-e89b-12d3-a456-426614174002")
    val id: MenuItemId,
    @Schema(example = "Margherita Pizza")
    var name: String,
    @Embedded
    var price: Money,
    @Enumerated(EnumType.STRING)
    @Schema(example = "AVAILABLE")
    var status: MenuItemStatus
) {
    @Column(name = "deleted")
    @Schema(example = "false")
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
