package mk.finki.restaurantservice.mcp

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/restaurants")
class RestaurantMcpController(
    private val mcpServer: RestaurantMcpServer
) {

    @GetMapping("/list", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun listRestaurants(): ResponseEntity<String> {
        val body = mcpServer.listRestaurants()
        return ResponseEntity.ok(body)
    }

    @GetMapping("/{restaurantId}/menu", produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getMenu(@PathVariable restaurantId: UUID): ResponseEntity<String> {
        val body = mcpServer.getRestaurantMenu(restaurantId)
        return ResponseEntity.ok(body)
    }

    data class IdsRequest(val ids: List<UUID>)

    @PostMapping("/check-availability", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun checkAvailability(@RequestBody req: IdsRequest): ResponseEntity<String> {
        val body = mcpServer.checkItemAvailability(req.ids)
        return ResponseEntity.ok(body)
    }
}
