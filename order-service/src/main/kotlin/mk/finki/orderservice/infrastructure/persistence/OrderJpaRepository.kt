package mk.finki.orderservice.infrastructure.persistence

import mk.finki.orderservice.domain.order.Order
import mk.finki.orderservice.domain.order.valueobjects.OrderId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderJpaRepository : JpaRepository<Order, OrderId>
