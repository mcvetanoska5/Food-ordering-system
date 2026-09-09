Restaurant MCP HTTP endpoints

This component exposes the MCP functionality over simple HTTP endpoints so external AI tools or test clients can query restaurant data.

Endpoints:
- GET /mcp/list-restaurants -> plain text list of restaurants (id: name at address)
- GET /mcp/menu/{restaurantId} -> plain text menu for given restaurant UUID
- POST /mcp/check-availability -> JSON body: { "ids": ["uuid1","uuid2"] } returns plain text availability lines

Integration:
- The controller delegates to RestaurantMcpServer component which uses application services.
- To test locally: run restaurant-service and call endpoints through the gateway or directly on the service port.

Role in architecture:
- Provides a simple, language-agnostic MCP-compatible surface for AI clients.
- Read-only operations intended for observation/extraction by external tools.
