package mk.finki.orderservice

import mk.finki.orderservice.domain.Order
import mk.finki.orderservice.domain.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID

class OrderStatusTransitionTest {

    @Test
    fun `allows legal transition`() {
        val order = Order(
            customerId = UUID.randomUUID(),
            restaurantId = UUID.randomUUID(),
            status = OrderStatus.CREATED
        )
        order.totalPrice = BigDecimal.TEN

        order.transitionTo(OrderStatus.CONFIRMED)

        assertThat(order.status).isEqualTo(OrderStatus.CONFIRMED)
    }

    @Test
    fun `rejects illegal transition`() {
        val order = Order(
            customerId = UUID.randomUUID(),
            restaurantId = UUID.randomUUID(),
            status = OrderStatus.CREATED
        )
        order.totalPrice = BigDecimal.TEN

        assertThatThrownBy { order.transitionTo(OrderStatus.DELIVERED) }
            .isInstanceOf(IllegalStateException::class.java)
    }
}
