package mk.finki.restaurantservice.domain;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface RestaurantRepository {
    Optional<Restaurant> findById(UUID id);
    List<Restaurant> findAll();
    Restaurant save(Restaurant restaurant);
}
