package mk.finki.restaurantservice.domain.restaurant.events

import mk.finki.restaurantservice.domain.restaurant.valueobjects.RestaurantId

abstract class RestaurantEvent(open val restaurantId: RestaurantId) : AbstractEvent(restaurantId)
