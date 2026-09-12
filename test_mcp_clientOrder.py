#!/usr/bin/env python3

"""
Terminal demo client for the order-service MCP server.

Connects over MCP Streamable HTTP, initializes a session, lists the
available tools, then calls each one in sequence:

    0. Fetch a real restaurant + menu item from restaurant-service
    1. placeOrder(...)
    2. getOrder(orderId)

Usage:
    pip install mcp
    python test_mcp_clientOrder.py
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
    """Extract the text payload from a CallToolResult."""

    for block in result.content:
        if hasattr(block, "text"):
            return block.text

    raise RuntimeError(
        f"No text content in tool result: {result}"
    )


def http_get_json(url: str):
    """Send a GET request and parse the JSON response."""

    with urllib.request.urlopen(url) as resp:
        return json.loads(
            resp.read().decode("utf-8")
        )


def fetch_restaurant_and_menu_item():
    """
    Fetch a real restaurant and menu item from restaurant-service.

    The restaurant endpoint already returns the restaurant together
    with its menu, so there is no need to call a separate
    /{restaurantId}/menu-items endpoint.
    """

    try:
        restaurants = http_get_json(
            RESTAURANT_REST_BASE
        )

    except urllib.error.URLError as e:
        print(
            f"\nCould not reach restaurant-service at "
            f"{RESTAURANT_REST_BASE}: {e}"
        )

        print(
            "Make sure restaurant-service is running "
            "on port 8082."
        )

        sys.exit(1)

    if not restaurants:
        print(
            "\nNo restaurants found in restaurant-service."
        )

        sys.exit(1)

    restaurant = restaurants[0]

    restaurant_id = restaurant["id"]

    menu = restaurant.get("menu", [])

    if not menu:
        print(
            f"\nRestaurant {restaurant_id} has no menu items."
        )

        sys.exit(1)

    menu_item_id = menu[0]["id"]

    return restaurant_id, menu_item_id


async def main() -> None:

    # ---------------------------------------------------------
    # 1. Fetch restaurant and menu item
    # ---------------------------------------------------------

    banner(
        "Fetching restaurant + menu item from restaurant-service"
    )

    restaurant_id, menu_item_id = (
        fetch_restaurant_and_menu_item()
    )

    print(
        f"restaurantId: {restaurant_id}"
    )

    print(
        f"menuItemId:   {menu_item_id}"
    )

    # Generate a customer ID for the demo
    customer_id = str(uuid.uuid4())

    print(
        f"customerId (generated): {customer_id}"
    )

    # ---------------------------------------------------------
    # 2. Connect to MCP server
    # ---------------------------------------------------------

    print(
        f"\nConnecting to order-service MCP server "
        f"at {ORDER_MCP_URL} ..."
    )

    async with streamable_http_client(
        ORDER_MCP_URL
    ) as (
        read_stream,
        write_stream
    ):

        async with ClientSession(
            read_stream,
            write_stream
        ) as session:

            # -------------------------------------------------
            # 3. Initialize MCP session
            # -------------------------------------------------

            banner(
                "Initializing MCP session"
            )

            init_result = await session.initialize()

            server = init_result.server_info

            print(
                f"Connected to '{server.name}' "
                f"v{server.version}"
            )

            print(
                f"Protocol version: "
                f"{init_result.protocol_version}"
            )

            # -------------------------------------------------
            # 4. List MCP tools
            # -------------------------------------------------

            banner(
                "Tools available"
            )

            tools_result = await session.list_tools()

            for tool in tools_result.tools:
                print(
                    f"- {tool.name}: "
                    f"{tool.description}"
                )

            # -------------------------------------------------
            # 5. Call placeOrder
            # -------------------------------------------------

            banner(
                "Calling placeOrder"
            )

            place_args = {
                "customerId": customer_id,
                "restaurantId": restaurant_id,
                "address": "123 Main St, New York, NY",
                "items": [
                    {
                        "menuItemId": menu_item_id,
                        "quantity": 2
                    }
                ],
            }

            print(
                "Arguments:"
            )

            print(
                json.dumps(
                    place_args,
                    indent=2
                )
            )

            place_result = await session.call_tool(
                "placeOrder",
                place_args
            )

            # -------------------------------------------------
            # 6. Check placeOrder result
            # -------------------------------------------------

            if place_result.is_error:

                print(
                    "\nplaceOrder failed:"
                )

                print(
                    place_result
                )

                for block in place_result.content:

                    if hasattr(block, "text"):

                        print(
                            f"TEXT: {block.text}"
                        )

                return

            # -------------------------------------------------
            # 7. Parse placeOrder response
            # -------------------------------------------------

            place_text = first_text(
                place_result
            )

            order = json.loads(
                place_text
            )

            print(
                "\n=== placeOrder response received ==="
            )

            print(
                json.dumps(
                    order,
                    indent=2
                )
            )

            # Get generated order ID
            order_id = order["id"]

            print(
                f"\nUsing orderId: {order_id}"
            )

            # -------------------------------------------------
            # 8. Call getOrder
            # -------------------------------------------------

            banner(
                "Calling getOrder"
            )

            get_result = await session.call_tool(
                "getOrder",
                {
                    "orderId": order_id
                }
            )

            # -------------------------------------------------
            # 9. Check getOrder result
            # -------------------------------------------------

            if get_result.is_error:

                print(
                    "\ngetOrder failed:"
                )

                print(
                    get_result
                )

                for block in get_result.content:

                    if hasattr(block, "text"):

                        print(
                            f"TEXT: {block.text}"
                        )

                return

            # -------------------------------------------------
            # 10. Parse getOrder response
            # -------------------------------------------------

            get_text = first_text(
                get_result
            )

            fetched_order = json.loads(
                get_text
            )

            print(
                "\n=== getOrder response received ==="
            )

            print(
                json.dumps(
                    fetched_order,
                    indent=2
                )
            )

    # ---------------------------------------------------------
    # 11. Demo finished
    # ---------------------------------------------------------

    banner(
        "Demo complete"
    )


if __name__ == "__main__":
    asyncio.run(main())