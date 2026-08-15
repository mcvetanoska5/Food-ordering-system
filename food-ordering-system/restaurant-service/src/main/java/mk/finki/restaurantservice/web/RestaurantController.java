package mk.finki.restaurantservice.web;

import lombok.RequiredArgsConstructor;
import mk.finki.restaurantservice.application.RestaurantApplicationService;
import mk.finki.restaurantservice.domain.Restaurant;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantApplicationService restaurantService;

    @GetMapping
    public List<Restaurant> getAll() {
        return restaurantService.listRestaurants();
    }

    @PostMapping("/{id}/menu-items")
    public void addMenuItem(@PathVariable UUID id, @RequestBody CreateMenuItemRequest request) {
        restaurantService.addMenuItem(id, request.name(), request.price());
    }

    public record CreateMenuItemRequest(String name, BigDecimal price) {}
}
