package mk.finki.restaurantservice.mcp

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.StaticToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallback
import org.springframework.ai.tool.support.ToolDefinitions
import org.springframework.ai.tool.metadata.ToolMetadata

/**
 * Registers @Tool-annotated bean methods with Spring AI's MCP server machinery.
 * This implementation creates MethodToolCallback entries for each annotated method and
 * registers them via a StaticToolCallbackProvider.
 */
@Configuration
@ConditionalOnClass(name = ["org.springframework.ai.tool.method.MethodToolCallback", "org.springframework.ai.tool.annotation.Tool"])
class McpSpringAiIntegration(private val restaurantAiTools: RestaurantAiTools) {

    @Bean
    fun toolCallbackProvider(): ToolCallbackProvider {
        // reflectively find the methods on RestaurantAiTools and create MethodToolCallback instances
        val listMethod = RestaurantAiTools::class.java.getMethod("listRestaurants")
        val getMenuMethod = RestaurantAiTools::class.java.getMethod("getMenu", java.util.UUID::class.java)
        val checkMethod = RestaurantAiTools::class.java.getMethod("checkAvailability", java.util.List::class.java)

        val listTool = MethodToolCallback.builder()
            .toolObject(restaurantAiTools)
            .toolMethod(listMethod)
            .toolDefinition(ToolDefinitions.from(listMethod))
            .toolMetadata(ToolMetadata.from(listMethod))
            .build()

        val getMenuTool = MethodToolCallback.builder()
            .toolObject(restaurantAiTools)
            .toolMethod(getMenuMethod)
            .toolDefinition(ToolDefinitions.from(getMenuMethod))
            .toolMetadata(ToolMetadata.from(getMenuMethod))
            .build()

        val checkTool = MethodToolCallback.builder()
            .toolObject(restaurantAiTools)
            .toolMethod(checkMethod)
            .toolDefinition(ToolDefinitions.from(checkMethod))
            .toolMetadata(ToolMetadata.from(checkMethod))
            .build()

        return StaticToolCallbackProvider(listOf(listTool, getMenuTool, checkTool))
    }
}
