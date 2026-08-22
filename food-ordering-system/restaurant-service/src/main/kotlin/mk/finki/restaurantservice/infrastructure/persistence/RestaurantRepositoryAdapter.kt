package mk.finki.restaurantservice.infrastructure.persistence

import mk.finki.restaurantservice.domain.restaurant.Restaurant
import mk.finki.restaurantservice.domain.restaurant.RestaurantRepository
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import org.springframework.stereotype.Component
import java.util.*

@Component
class RestaurantRepositoryAdapter(
    private val jpaRepository: RestaurantJpaRepository
) : RestaurantRepository {
    override fun findById(id: RestaurantId): Optional<Restaurant> = jpaRepository.findById(id)
    override fun findAll(): List<Restaurant> = jpaRepository.findAll()
    override fun save(restaurant: Restaurant): Restaurant = jpaRepository.save(restaurant)
}
