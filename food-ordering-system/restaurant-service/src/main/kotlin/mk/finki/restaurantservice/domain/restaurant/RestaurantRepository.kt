package mk.finki.restaurantservice.domain.restaurant

import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import java.util.*

interface RestaurantRepository {
    fun findById(id: RestaurantId): Optional<Restaurant>
    fun findAll(): List<Restaurant>
    fun save(restaurant: Restaurant): Restaurant
}
