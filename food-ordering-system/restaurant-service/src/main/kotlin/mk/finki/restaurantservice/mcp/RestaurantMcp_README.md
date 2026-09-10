Restaurant MCP server

This service exposes an actual Model Context Protocol (MCP) server, backed by
`spring-ai-starter-mcp-server-webmvc` (Spring AI 1.1.0), over the Streamable HTTP
transport (`spring.ai.mcp.server.protocol: STREAMABLE`, see `application-test.yml`).

MCP endpoint (JSON-RPC 2.0 over HTTP, one endpoint for POST/GET/DELETE per the
MCP Streamable HTTP spec):

    POST/GET/DELETE http://localhost:8082/mcp

Tools registered (defined in `RestaurantAiTools.kt`, wired to the MCP server via
`McpSpringAiIntegration.kt`):
- `listRestaurants()` -> JSON array of restaurants (id, name, address)
- `getMenu(restaurantId: UUID)` -> JSON array of menu items for that restaurant
- `checkAvailability(ids: List<UUID>)` -> JSON array of { menuItemId, available }

Testing from the terminal:
- `pip install mcp`, then run `test_mcp_client.py` at the repo root — it connects
  over Streamable HTTP, initializes a session, lists the tools, and calls all
  three in sequence using ids chained from the previous call's result.
- Or probe manually with curl:
  `curl -X POST http://localhost:8082/mcp -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"probe","version":"0.0.1"}}}'`

Separate plain-HTTP surface:
`RestaurantMcpController.kt` also exposes the same underlying data as plain
REST/text endpoints under `/api/restaurants/...` for tools that can't speak
MCP/JSON-RPC directly. This is not the MCP protocol itself — it delegates to
the same `RestaurantMcpServer` component. See that controller's KDoc for routes.

Role in architecture:
- Read-only operations intended for observation/extraction by external AI
  clients, wrapping the existing `RestaurantApplicationService` /
  `MenuItemApplicationService` layer — no business logic is duplicated.
