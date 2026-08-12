package mk.finki.orderservice.exception

import java.util.UUID

/** Thrown when the Restaurant Service reports items as unavailable/nonexistent. */
class InvalidOrderException(val unavailableItems: List<UUID>) :
    RuntimeException("Order rejected: one or more items are unavailable: $unavailableItems")
