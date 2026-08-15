package mk.finki.restaurantservice.application;

import lombok.RequiredArgsConstructor;
import mk.finki.restaurantservice.domain.Restaurant;
import mk.finki.restaurantservice.domain.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantApplicationService {
    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<Restaurant> listRestaurants() {
        return restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Restaurant getRestaurant(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    @Transactional
    public void addMenuItem(UUID restaurantId, String name, BigDecimal price) {
        Restaurant restaurant = getRestaurant(restaurantId);
        restaurant.addMenuItem(name, price);
        restaurantRepository.save(restaurant);
    }
}
