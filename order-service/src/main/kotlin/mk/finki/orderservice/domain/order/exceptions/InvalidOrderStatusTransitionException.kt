package mk.finki.orderservice.domain.order.exceptions

import mk.finki.orderservice.domain.order.enums.OrderStatus

class InvalidOrderStatusTransitionException(current: OrderStatus, target: OrderStatus) :
    RuntimeException("Cannot transition from $current to $target")
