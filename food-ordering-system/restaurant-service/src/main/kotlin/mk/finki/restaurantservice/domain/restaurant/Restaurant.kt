package mk.finki.restaurantservice.domain.restaurant

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
    val id: RestaurantId,
    var name: String,
    var address: String
) {
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private val _menu: MutableList<MenuItem> = mutableListOf()

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
