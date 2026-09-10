package mk.finki.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RouteConfig {

    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("restaurant-service") { r ->
                r.path("/api/restaurants/**", "/api/menu-items/**")
                    .uri("lb://restaurant-service")
            }
            .route("order-service") { r ->
                r.path("/api/orders/**")
                    .uri("lb://order-service")
            }
            .build()
    }
}
