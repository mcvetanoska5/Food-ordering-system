package mk.finki.orderservice.client

import mk.finki.orderservice.dto.ValidateItemsRequest
import mk.finki.orderservice.dto.ValidateItemsResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

/**
 * Synchronous inter-service call to the Restaurant & Catalog Service.
 * "restaurant-service" is resolved via Consul service discovery (service registry) —
 * no hardcoded host/port, so this works the same locally and in the cluster.
 *
 * This is exactly the call the Pact consumer contract test (see src/test) verifies.
 */
@FeignClient(
    name = "restaurant-service",
    fallback = RestaurantClientFallback::class
)
interface RestaurantClient {

    @PostMapping("/menu/items/validate")
    fun validateItems(@RequestBody request: ValidateItemsRequest): ValidateItemsResponse
}
