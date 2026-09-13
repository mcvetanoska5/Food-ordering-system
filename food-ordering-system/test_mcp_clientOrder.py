#!/usr/bin/env python3
"""
Terminal demo client for the order-service MCP server.

Connects over MCP Streamable HTTP (spring-ai-starter-mcp-server-webmvc, protocol=STREAMABLE),
initializes a session, lists the available tools, then calls each one in sequence:

    0. (plain REST, not MCP) fetch a restaurant + menu item from restaurant-service
       so placeOrder has a real restaurantId/menuItemId to work with
    1. placeOrder(customerId, restaurantId, address, items=[{menuItemId, quantity}])
    2. getOrder(orderId)   using the id returned by placeOrder

Usage:
    pip install mcp
    python test_mcp_client_order.py
"""
import asyncio
import json
import sys
import urllib.error
import urllib.request
import uuid

from mcp import ClientSession
from mcp.client.streamable_http import streamable_http_client

ORDER_MCP_URL = "http://localhost:8081/mcp"
RESTAURANT_REST_BASE = "http://localhost:8082/api/restaurants"


def banner(title: str) -> None:
    print(f"\n=== {title} ===")


def first_text(result) -> str:
    """Extract the text payload from a CallToolResult (Spring AI returns one TextContent block)."""
    for block in result.content:
        if hasattr(block, "text"):
            return block.text
    raise RuntimeError(f"No text content in tool result: {result}")


def http_get_json(url: str):
    with urllib.request.urlopen(url) as resp:
        return json.loads(resp.read().decode("utf-8"))


def fetch_restaurant_and_menu_item():
    """
    Pulls a real restaurant + menu item straight from restaurant-service's plain
    REST surface (not MCP), so placeOrder below has valid ids to work with.
    """
    try:
        restaurants = http_get_json(f"{RESTAURANT_REST_BASE}")
    except urllib.error.URLError as e:
        print(f"\nCould not reach restaurant-service at {RESTAURANT_REST_BASE}: {e}")
        print("Make sure restaurant-service is running on port 8082.")
        sys.exit(1)

    if not restaurants:
        print("\nNo restaurants found in restaurant-service. Seed one first, e.g.:")
        print('  curl -X POST http://localhost:8082/api/restaurants '
              '-H "Content-Type: application/json" '
              '-d "{\\"name\\":\\"Pizza Palace\\",\\"address\\":\\"456 Oak St\\"}"')
        sys.exit(1)

    restaurant_id = restaurants[0]["id"]

    menu = http_get_json(f"{RESTAURANT_REST_BASE}/{restaurant_id}/menu-items")
    if not menu:
        print(f"\nRestaurant {restaurant_id} has no menu items. Add one first, e.g.:")
        print(f'  curl -X POST http://localhost:8082/api/restaurants/{restaurant_id}/menu-items '
              '-H "Content-Type: application/json" '
              '-d "{\\"name\\":\\"Margherita Pizza\\",\\"price\\":12.99,\\"currency\\":\\"USD\\"}"')
        sys.exit(1)

    menu_item_id = menu[0]["id"]
    return restaurant_id, menu_item_id


async def main() -> None:
    banner("Fetching restaurant + menu item from restaurant-service")
    restaurant_id, menu_item_id = fetch_restaurant_and_menu_item()
    print(f"restaurantId: {restaurant_id}")
    print(f"menuItemId:   {menu_item_id}")

    customer_id = str(uuid.uuid4())
    print(f"customerId (generated): {customer_id}")

    print(f"\nConnecting to order-service MCP server at {ORDER_MCP_URL} ...")

    async with streamable_http_client(ORDER_MCP_URL) as (read_stream, write_stream):
        async with ClientSession(read_stream, write_stream) as session:

            banner("Initializing MCP session")
            init_result = await session.initialize()
            server = init_result.server_info
            print(f"Connected to '{server.name}' v{server.version}")
            print(f"Protocol version: {init_result.protocol_version}")

            banner("Tools available")
            tools_result = await session.list_tools()
            for tool in tools_result.tools:
                print(f"- {tool.name}: {tool.description}")

            # 1. placeOrder
            banner("Calling placeOrder")
            place_args = {
                "customerId": customer_id,
                "restaurantId": restaurant_id,
                "address": "123 Main St, New York, NY",
                "items": [{"menuItemId": menu_item_id, "quantity": 2}],
            }
            print(f"Arguments: {json.dumps(place_args, indent=2)}")
            place_result = await session.call_tool("placeOrder", place_args)
            order = json.loads(first_text(place_result))
            print(json.dumps(order, indent=2))

            order_id = order["id"]
            print(f"\nUsing orderId: {order_id}")

            # 2. getOrder
            banner("Calling getOrder")
            get_result = await session.call_tool("getOrder", {"orderId": order_id})
            fetched_order = json.loads(first_text(get_result))
            print(json.dumps(fetched_order, indent=2))

    banner("Demo complete")


if __name__ == "__main__":
    asyncio.run(main())