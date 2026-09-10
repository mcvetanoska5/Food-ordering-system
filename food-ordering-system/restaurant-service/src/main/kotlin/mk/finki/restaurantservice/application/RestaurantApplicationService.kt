package mk.finki.restaurantservice.application

import mk.finki.restaurantservice.domain.restaurant.Restaurant
import mk.finki.restaurantservice.domain.restaurant.RestaurantRepository
import mk.finki.restaurantservice.domain.restaurant.valueobjects.Money
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RestaurantApplicationService(
    private val restaurantRepository: RestaurantRepository
) {
    @Transactional(readOnly = true)
    fun listRestaurants(): List<Restaurant> = restaurantRepository.findAll()

    @Transactional(readOnly = true)
    fun getRestaurant(id: UUID): Optional<Restaurant> = restaurantRepository.findById(RestaurantId(id))

    @Transactional
    fun createRestaurant(name: String, address: String): Restaurant {
        val restaurant = Restaurant(RestaurantId(UUID.randomUUID()), name, address)
        return restaurantRepository.save(restaurant)
    }

    @Transactional
    fun addMenuItem(restaurantId: UUID, name: String, amount: java.math.BigDecimal, currency: String): mk.finki.restaurantservice.domain.restaurant.entities.MenuItem {
        val restaurant = restaurantRepository.findById(RestaurantId(restaurantId))
            .orElseThrow { NoSuchElementException("Restaurant not found: $restaurantId") }
        val menuItem = restaurant.addMenuItem(name, Money(amount, currency))
        restaurantRepository.save(restaurant)
        return menuItem
    }

    @Transactional
    fun updateMenuItem(restaurantId: UUID, menuItemId: UUID, name: String?, price: java.math.BigDecimal?, status: mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus?) {
        val restaurant = restaurantRepository.findById(RestaurantId(restaurantId))
            .orElseThrow { NoSuchElementException("Restaurant not found: $restaurantId") }
        val menuItem = restaurant.menu.find { it.id.value == menuItemId }
            ?: throw NoSuchElementException("Menu item not found: $menuItemId")

        name?.let { menuItem.name = it }
        price?.let { menuItem.updatePrice(Money(it, menuItem.price.currency)) }
        status?.let { menuItem.updateStatus(it) }

        restaurantRepository.save(restaurant)
    }

    @Transactional
    fun deleteMenuItem(restaurantId: UUID, menuItemId: UUID) {
        val restaurant = restaurantRepository.findById(RestaurantId(restaurantId))
            .orElseThrow { NoSuchElementException("Restaurant not found: $restaurantId") }
        val menuItem = restaurant.menu.find { it.id.value == menuItemId }
            ?: throw NoSuchElementException("Menu item not found: $menuItemId")
        
        menuItem.discontinue()
        restaurantRepository.save(restaurant)
    }
}
