package mk.finki.restaurantservice.mcp;

import lombok.RequiredArgsConstructor;
import mk.finki.restaurantservice.application.RestaurantApplicationService;
import mk.finki.restaurantservice.domain.Restaurant;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simplified MCP Server implementation for demonstration.
 * In a real scenario, this would use an MCP SDK or expose a JSON-RPC over stdio/HTTP.
 */
@Component
@RequiredArgsConstructor
public class RestaurantMcpServer {
    private final RestaurantApplicationService restaurantService;

    // MCP Tools & Resources
    
    public List<Map<String, Object>> listRestaurants() {
        return restaurantService.listRestaurants().stream()
                .map(r -> Map.of("id", r.getId(), "name", r.getName()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getRestaurantMenu(UUID restaurantId) {
        Restaurant r = restaurantService.getRestaurant(restaurantId);
        return Map.of(
            "restaurantName", r.getName(),
            "menu", r.getMenu().stream()
                .map(item -> Map.of(
                    "id", item.getId(),
                    "name", item.getName(),
                    "price", item.getPrice(),
                    "status", item.getStatus()
                )).collect(Collectors.toList())
        );
    }
}
