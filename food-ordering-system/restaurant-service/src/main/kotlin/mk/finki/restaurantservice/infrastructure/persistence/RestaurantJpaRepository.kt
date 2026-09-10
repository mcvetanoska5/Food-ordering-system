package mk.finki.restaurantservice.infrastructure.persistence

import mk.finki.restaurantservice.domain.restaurant.Restaurant
import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RestaurantJpaRepository : JpaRepository<Restaurant, RestaurantId>
