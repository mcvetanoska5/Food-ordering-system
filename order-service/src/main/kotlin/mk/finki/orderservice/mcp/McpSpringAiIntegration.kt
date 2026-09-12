package mk.finki.orderservice.mcp

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.StaticToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallback
import org.springframework.ai.tool.support.ToolDefinitions
import org.springframework.ai.tool.metadata.ToolMetadata
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

/**
 * Registers @Tool-annotated bean methods with Spring AI's MCP server machinery.
 * This implementation creates MethodToolCallback entries for each annotated method and
 * registers them via a StaticToolCallbackProvider.
 */
@Configuration
@ConditionalOnClass(name = ["org.springframework.ai.tool.method.MethodToolCallback", "org.springframework.ai.tool.annotation.Tool"])
class McpSpringAiIntegration(private val orderAiTools: OrderAiTools) {

    @Bean
    fun toolCallbackProvider(): ToolCallbackProvider {
        // reflectively find the methods on OrderAiTools and create MethodToolCallback instances
        val placeOrderMethod = OrderAiTools::class.java.getMethod(
            "placeOrder",
            UUID::class.java,
            UUID::class.java,
            String::class.java,
            java.util.List::class.java
        )
        val getOrderMethod = OrderAiTools::class.java.getMethod("getOrder", UUID::class.java)

        val placeOrderTool = MethodToolCallback.builder()
            .toolObject(orderAiTools)
            .toolMethod(placeOrderMethod)
            .toolDefinition(ToolDefinitions.from(placeOrderMethod))
            .toolMetadata(ToolMetadata.from(placeOrderMethod))
            .build()

        val getOrderTool = MethodToolCallback.builder()
            .toolObject(orderAiTools)
            .toolMethod(getOrderMethod)
            .toolDefinition(ToolDefinitions.from(getOrderMethod))
            .toolMetadata(ToolMetadata.from(getOrderMethod))
            .build()

        return StaticToolCallbackProvider(listOf(placeOrderTool, getOrderTool))
    }
}