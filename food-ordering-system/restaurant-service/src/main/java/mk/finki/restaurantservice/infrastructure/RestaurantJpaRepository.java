package mk.finki.restaurantservice.infrastructure;

import mk.finki.restaurantservice.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RestaurantJpaRepository extends JpaRepository<Restaurant, UUID> {
}
