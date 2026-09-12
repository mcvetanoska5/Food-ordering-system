package mk.finki.restaurantservice.exception

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class ErrorResponse(
    @Schema(example = "2023-10-27T10:00:00Z")
    val timestamp: String = Instant.now().toString(),
    @Schema(example = "404")
    val status: Int,
    @Schema(example = "Not Found")
    val error: String,
    @Schema(example = "Restaurant not found: 123e4567-e89b-12d3-a456-426614174000")
    val message: String,
    @Schema(example = "/api/restaurants/123e4567-e89b-12d3-a456-426614174000")
    val path: String
)
