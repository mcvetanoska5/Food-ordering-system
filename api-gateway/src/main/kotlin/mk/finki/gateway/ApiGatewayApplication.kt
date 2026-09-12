package mk.finki.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

/**
 * API Gateway entry point for the Food Ordering System.
 * Order endpoints are forwarded to order-service.
 * Restaurant endpoints are forwarded to restaurant-service.
 * Requests are validated through Keycloak at the edge.
 */
@SpringBootApplication
@EnableDiscoveryClient
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}
