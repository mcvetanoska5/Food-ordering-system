package mk.finki.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * API Gateway — centralized entry point for the Food Ordering System.
 * Routes /api/orders/**       -> order-service
 *        /api/restaurants/**  -> restaurant-service
 * Both resolved dynamically via Consul (service discovery), not hardcoded hosts.
 * Also enforces JWT validation (Keycloak) at the edge before requests reach services.
 */
@SpringBootApplication
@EnableDiscoveryClient
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
