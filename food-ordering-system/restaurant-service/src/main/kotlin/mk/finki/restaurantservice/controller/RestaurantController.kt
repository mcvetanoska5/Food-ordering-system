package mk.finki.restaurantservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mk.finki.restaurantservice.application.RestaurantApplicationService
import mk.finki.restaurantservice.domain.restaurant.Restaurant
import mk.finki.restaurantservice.domain.restaurant.entities.MenuItem
import mk.finki.restaurantservice.domain.restaurant.enums.MenuItemStatus
import mk.finki.restaurantservice.exception.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/api/restaurants")
@SecurityRequirement(name = "bearerAuth")
class RestaurantController(
    private val restaurantService: RestaurantApplicationService
) {
    @GetMapping
    @Operation(summary = "List all restaurants with optional pagination")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "List of restaurants", content = [Content(array = ArraySchema(schema = Schema(implementation = Restaurant::class)))]),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Restaurants not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    fun getAll(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): List<Restaurant> {
        val all = restaurantService.listRestaurants()
        if (page == null || size == null) return all
        return all.drop(page * size).take(size)
    }

    @PostMapping
    @Operation(summary = "Create a new restaurant")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "Restaurant created",
            headers = [Header(name = "Location", description = "URI of the created restaurant", schema = Schema(type = "string"))],
            content = [Content(mediaType = "application/json", schema = Schema(implementation = Restaurant::class))]
        ),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Restaurant not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateRestaurantRequest::class))]
    )
    fun createRestaurant(@RequestBody request: CreateRestaurantRequest): ResponseEntity<Restaurant> {
        val restaurant = restaurantService.createRestaurant(request.name, request.address)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(restaurant.id.value)
            .toUri()
        return ResponseEntity.created(location).body(restaurant)
    }

    @PostMapping("/{id}/menu-items")
    @Operation(summary = "Add a menu item to a restaurant")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "201",
            description = "Menu item added",
            headers = [Header(name = "Location", description = "URI of the created menu item", schema = Schema(type = "string"))],
            content = [Content(mediaType = "application/json", schema = Schema(implementation = MenuItem::class))]
        ),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Restaurant not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CreateMenuItemRequest::class))]
    )
    fun addMenuItem(
        @PathVariable id: UUID,
        @RequestBody request: CreateMenuItemRequest
    ): ResponseEntity<MenuItem> {
        val menuItem = restaurantService.addMenuItem(id, request.name, request.price, request.currency)
        val location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{menuItemId}")
            .buildAndExpand(menuItem.id.value)
            .toUri()
        return ResponseEntity.created(location).body(menuItem)
    }

    @PatchMapping("/{id}/menu-items/{menuItemId}")
    @Operation(summary = "Update a menu item")
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Menu item updated", content = [Content(mediaType = "application/json", schema = Schema(implementation = MenuItem::class))]),
        ApiResponse(responseCode = "204", description = "Menu item updated without body"),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Restaurant or menu item not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        required = true,
        content = [Content(mediaType = "application/json", schema = Schema(implementation = UpdateMenuItemRequest::class))]
    )
    fun updateMenuItem(
        @PathVariable id: UUID,
        @PathVariable menuItemId: UUID,
        @RequestBody request: UpdateMenuItemRequest
    ): ResponseEntity<MenuItem> {
        restaurantService.updateMenuItem(id, menuItemId, request.name, request.price, request.status)
        val restaurant = restaurantService.getRestaurant(id).orElseThrow { NoSuchElementException("Restaurant not found: $id") }
        val updated = restaurant.menu.find { it.id.value == menuItemId }
            ?: throw NoSuchElementException("Menu item not found: $menuItemId")
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/{id}/menu-items/{menuItemId}")
    @Operation(summary = "Delete a menu item (soft delete)", description = "Marks the menu item as deleted without removing it from the restaurant catalogue.")
    @ApiResponses(value = [
        ApiResponse(responseCode = "204", description = "Menu item deleted"),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "Restaurant or menu item not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMenuItem(
        @PathVariable id: UUID,
        @PathVariable menuItemId: UUID
    ) {
        restaurantService.deleteMenuItem(id, menuItemId)
    }

    data class CreateRestaurantRequest(
        @Schema(example = "Pizza Palace")
        val name: String,
        @Schema(example = "456 Oak St, London")
        val address: String
    )

    data class CreateMenuItemRequest(
        @Schema(example = "Margherita Pizza")
        val name: String,
        @Schema(example = "12.99")
        val price: BigDecimal,
        @Schema(example = "USD")
        val currency: String = "USD"
    )

    data class UpdateMenuItemRequest(
        @Schema(example = "Updated Pizza Name")
        val name: String? = null,
        @Schema(example = "15.99")
        val price: BigDecimal? = null,
        val status: MenuItemStatus? = null
    )
}
