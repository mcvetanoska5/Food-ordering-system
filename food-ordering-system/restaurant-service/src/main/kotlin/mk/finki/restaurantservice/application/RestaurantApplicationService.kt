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
    fun addMenuItem(restaurantId: UUID, name: String, amount: java.math.BigDecimal, currency: String) {
        val restaurant = restaurantRepository.findById(RestaurantId(restaurantId))
            .orElseThrow { RuntimeException("Restaurant not found") }
        restaurant.addMenuItem(name, Money(amount, currency))
        restaurantRepository.save(restaurant)
    }
}
