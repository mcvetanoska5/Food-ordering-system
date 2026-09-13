package mk.finki.restaurantservice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import mk.finki.restaurantservice.application.MenuItemApplicationService
import mk.finki.restaurantservice.controller.dto.MenuItemAvailabilityResponse
import mk.finki.restaurantservice.exception.ErrorResponse
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/menu-items")
@SecurityRequirement(name = "bearerAuth")
class MenuItemController(
    private val menuItemApplicationService: MenuItemApplicationService
) {
    @GetMapping("/availability")
    @Operation(summary = "Check availability and price for menu items")
    @ApiResponses(value = [
        ApiResponse(
            responseCode = "200",
            description = "Availability info retrieved",
            content = [
                Content(
                    array = ArraySchema(schema = Schema(implementation = MenuItemAvailabilityResponse::class)),
                    examples = [ExampleObject(value = """
                        [
                          {
                            "menuItemId": "123e4567-e89b-12d3-a456-426614174002",
                            "available": true,
                            "price": 12.99,
                            "currency": "USD"
                          }
                        ]
                        """)]
                )
            ]
        ),
        ApiResponse(responseCode = "400", description = "Invalid request", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "401", description = "Unauthorized", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "403", description = "Forbidden", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
        ApiResponse(responseCode = "404", description = "One or more menu items not found", content = [Content(schema = Schema(implementation = ErrorResponse::class))])
    ])
    fun checkAvailability(@RequestParam ids: List<UUID>): List<MenuItemAvailabilityResponse> {
        val availability = menuItemApplicationService.checkAvailability(ids)
        return availability.map { MenuItemAvailabilityResponse(it.menuItemId, it.available, it.price, it.currency) }
    }
}
