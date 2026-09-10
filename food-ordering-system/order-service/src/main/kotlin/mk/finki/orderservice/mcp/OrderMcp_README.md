# Order MCP server

This service exposes an actual Model Context Protocol (MCP) server, backed by
`spring-ai-starter-mcp-server-webmvc` (Spring AI 1.1.0), over the Streamable HTTP
transport (`spring.ai.mcp.server.protocol: STREAMABLE`, see `application-test.yml`).

MCP endpoint (JSON-RPC 2.0 over HTTP, one endpoint for POST/GET/DELETE per the
MCP Streamable HTTP spec):

    POST/GET/DELETE http://localhost:8081/mcp

Tools registered (defined in `OrderAiTools.kt`, wired to the MCP server via
`McpSpringAiIntegration.kt`):
- `placeOrder(customerId: UUID, restaurantId: UUID, address: String, items: List<{menuItemId, quantity}>)`
  -> places a new order (checks item availability against restaurant-service first)
  and returns a JSON object with id, customerId, restaurantId, address, status,
  items and totalPrice
- `getOrder(orderId: UUID)` -> JSON object with the same shape, for an existing order

Testing from the terminal:
- `pip install mcp`, then run `test_mcp_client.py` at the repo root — it connects
  over Streamable HTTP, initializes a session, lists the tools, fetches a real
  restaurant + menu item from restaurant-service, then calls `placeOrder` and
  `getOrder` in sequence using the id returned by `placeOrder`.
- Or probe manually with curl:
  `curl -X POST http://localhost:8081/mcp -H "Content-Type: application/json" -H "Accept: application/json, text/event-stream" -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"probe","version":"0.0.1"}}}'`

Separate plain-HTTP surface:
`OrderMcpController.kt` also exposes the same underlying data as plain
REST/text endpoints under `/api/orders/place` and `/api/orders/{orderId}/summary`
for tools that can't speak MCP/JSON-RPC directly. This is not the MCP protocol
itself — it delegates to the same `OrderMcpServer` component. See that
controller's KDoc for routes.

Role in architecture:
- `placeOrder` and `getOrder` wrap the existing `OrderApplicationService`
  layer — no business logic is duplicated. `placeOrder` still goes through
  the real flow (Feign call to restaurant-service to check availability,
  persistence, and the `OrderPlacedEvent` published via
  `EventMessagingEventHandler`), so it is a genuine write operation exposed
  to AI clients, not a read-only observation tool like restaurant-service's.
- Because `placeOrder` calls out to restaurant-service, restaurant-service
  must be running and reachable for this tool to succeed.