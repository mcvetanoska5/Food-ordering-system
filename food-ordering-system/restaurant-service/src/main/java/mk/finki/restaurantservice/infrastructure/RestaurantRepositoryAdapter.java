package mk.finki.restaurantservice.infrastructure;

import lombok.RequiredArgsConstructor;
import mk.finki.restaurantservice.domain.Restaurant;
import mk.finki.restaurantservice.domain.RestaurantRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantRepositoryAdapter implements RestaurantRepository {
    private final RestaurantJpaRepository jpaRepository;

    @Override
    public Optional<Restaurant> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Restaurant> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Restaurant save(Restaurant restaurant) {
        return jpaRepository.save(restaurant);
    }
}
