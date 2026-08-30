package mk.finki.restaurantservice.domain.restaurant

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import mk.finki.restaurantservice.domain.restaurant.entities.MenuItem
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.domain.restaurant.exceptions.DuplicateMenuItemException
import mk.finki.restaurantservice.domain.restaurant.valueobjects.MenuItemId
import mk.finki.restaurantservice.domain.restaurant.valueobjects.Money
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import java.util.UUID

@Entity
@Table(name = "restaurants")
class Restaurant(
    @EmbeddedId
    @Schema(example = "123e4567-e89b-12d3-a456-426614174000")
    val id: RestaurantId,
    @Schema(example = "Pizza Palace")
    var name: String,
    @Schema(example = "456 Oak St, London")
    var address: String
) {
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private val _menu: MutableList<MenuItem> = mutableListOf()

    @Schema(example = "[]")
    val menu: List<MenuItem>
        get() = _menu.filter { !it.deleted }

    fun addMenuItem(name: String, price: Money): MenuItem {
        if (_menu.any { it.name == name && !it.deleted }) {
            throw DuplicateMenuItemException("Menu item with name $name already exists in restaurant ${this.name}")
        }
        val menuItem = MenuItem(
            id = MenuItemId(UUID.randomUUID()),
            name = name,
            price = price,
            status = MenuItemStatus.AVAILABLE
        )
        _menu.add(menuItem)
        return menuItem
    }

    fun discontinueMenuItem(menuItemId: MenuItemId) {
        _menu.find { it.id == menuItemId }?.discontinue()
    }
    
    // For JPA
    protected constructor() : this(RestaurantId(UUID.randomUUID()), "", "")
}
