#!/usr/bin/env python3
"""
Terminal demo client for the restaurant-service MCP server.

Connects over MCP Streamable HTTP (spring-ai-starter-mcp-server-webmvc, protocol=STREAMABLE),
initializes a session, lists the available tools, then calls each one in sequence:

    1. listRestaurants                (no args)
    2. getMenu(restaurantId=...)      using an id from step 1
    3. checkAvailability(ids=[...])   using a menu item id from step 2

Usage:
    pip install mcp
    python test_mcp_client.py
"""
import asyncio
import json
import sys

from mcp import ClientSession
from mcp.client.streamable_http import streamable_http_client

MCP_URL = "http://localhost:8082/mcp"


def banner(title: str) -> None:
    print(f"\n=== {title} ===")


def first_text(result) -> str:
    """Extract the text payload from a CallToolResult (Spring AI returns one TextContent block)."""
    for block in result.content:
        if hasattr(block, "text"):
            return block.text
    raise RuntimeError(f"No text content in tool result: {result}")


async def main() -> int:
    print(f"Connecting to MCP server at {MCP_URL} ...")

    async with streamable_http_client(MCP_URL) as (read_stream, write_stream):
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

            # 1. listRestaurants
            banner("Calling listRestaurants")
            list_result = await session.call_tool("listRestaurants", {})
            restaurants = json.loads(first_text(list_result))
            print(json.dumps(restaurants, indent=2))

            if not restaurants:
                print("\nNo restaurants found. Seed one first, e.g.:")
                print('  curl -X POST http://localhost:8082/api/restaurants '
                      '-H "Content-Type: application/json" '
                      '-d "{\\"name\\":\\"Pizza Palace\\",\\"address\\":\\"456 Oak St\\"}"')
                return 1

            restaurant_id = restaurants[0]["id"]
            print(f"\nUsing restaurantId: {restaurant_id}")

            # 2. getMenu
            banner("Calling getMenu")
            menu_result = await session.call_tool("getMenu", {"restaurantId": restaurant_id})
            menu = json.loads(first_text(menu_result))
            print(json.dumps(menu, indent=2))

            if not menu:
                print("\nRestaurant has no menu items. Add one first, e.g.:")
                print(f'  curl -X POST http://localhost:8082/api/restaurants/{restaurant_id}/menu-items '
                      '-H "Content-Type: application/json" '
                      '-d "{\\"name\\":\\"Margherita Pizza\\",\\"price\\":12.99,\\"currency\\":\\"USD\\"}"')
                return 1

            menu_item_id = menu[0]["id"]
            print(f"\nUsing menuItemId: {menu_item_id}")

            # 3. checkAvailability
            banner("Calling checkAvailability")
            availability_result = await session.call_tool("checkAvailability", {"ids": [menu_item_id]})
            availability = json.loads(first_text(availability_result))
            print(json.dumps(availability, indent=2))

    banner("Demo complete")
    return 0


if __name__ == "__main__":
    sys.exit(asyncio.run(main()))
