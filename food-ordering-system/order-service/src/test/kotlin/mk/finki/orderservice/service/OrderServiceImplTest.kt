package mk.finki.orderservice.service

import mk.finki.orderservice.client.RestaurantClient
import mk.finki.orderservice.domain.Order
import mk.finki.orderservice.domain.OrderStatus
import mk.finki.orderservice.dto.CreateOrderRequest
import mk.finki.orderservice.dto.OrderItemRequest
import mk.finki.orderservice.dto.ValidateItemsRequest
import mk.finki.orderservice.dto.ValidateItemsResponse
import mk.finki.orderservice.exception.InvalidOrderException
import mk.finki.orderservice.exception.OrderNotFoundException
import mk.finki.orderservice.messaging.OrderCreatedEvent
import mk.finki.orderservice.messaging.OrderEventProducer
import mk.finki.orderservice.messaging.OrderStatusChangedEvent
import mk.finki.orderservice.repository.OrderRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.util.Optional
import java.util.UUID

/**
 * Integration test for the OrderServiceImpl "create order" flow.
 *
 * This intentionally does NOT spin up a full Spring context (no real DB, Kafka,
 * Consul, or Keycloak needed) — it wires OrderServiceImpl directly against mocked
 * collaborators. This still verifies real cross-component behavior that a plain
 * unit test on the Order aggregate wouldn't catch:
 *   1. RestaurantClient is called with the correct validation payload
 *   2. An invalid response (unavailable items) rejects the order and never saves it
 *   3. A valid response causes price/name to be SNAPSHOTTED from the validation
 *      response (not trusted from the client's request)
 *   4. The saved order is persisted and an OrderCreatedEvent is published to Kafka
 *   5. Status transitions publish OrderStatusChangedEvent with correct before/after
 */
class OrderServiceImplTest {

    private val orderRepository: OrderRepository = mock()
    private val restaurantClient: RestaurantClient = mock()
    private val eventProducer: OrderEventProducer = mock()

    private lateinit var orderService: OrderServiceImpl

    private val restaurantId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val menuItemId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        orderService = OrderServiceImpl(orderRepository, restaurantClient, eventProducer)
    }

    @Test
    fun `creates order with snapshotted price and name when items are valid`() {
        val request = CreateOrderRequest(
            customerId = customerId,
            restaurantId = restaurantId,
            items = listOf(OrderItemRequest(menuItemId, 2)),
            deliveryAddress = "Скопје, ул. Македонија"
        )

        whenever(restaurantClient.validateItems(any())).thenReturn(
            ValidateItemsResponse(
                valid = true,
                validatedItems = listOf(
                    ValidateItemsResponse.MenuItemValidation(
                        menuItemId = menuItemId,
                        name = "Margherita Pizza",
                        currentPrice = BigDecimal("350.00"),
                        available = true
                    )
                ),
                unavailableItems = emptyList()
            )
        )

        // save() just returns whatever it's given, like a real repository would after INSERT
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

        val response = orderService.createOrder(request)

        // 1. Restaurant Service was asked to validate with the right restaurant/items
        val requestCaptor = argumentCaptor<ValidateItemsRequest>()
        verify(restaurantClient).validateItems(requestCaptor.capture())
        assertThat(requestCaptor.firstValue.restaurantId).isEqualTo(restaurantId)
        assertThat(requestCaptor.firstValue.items).containsExactly(
            ValidateItemsRequest.Item(menuItemId, 2)
        )

        // 2. Price/name came from the validation response, not from the caller's request
        assertThat(response.items).hasSize(1)
        assertThat(response.items[0].itemName).isEqualTo("Margherita Pizza")
        assertThat(response.items[0].unitPrice).isEqualByComparingTo("350.00")
        assertThat(response.totalPrice).isEqualByComparingTo("700.00") // 350 * 2
        assertThat(response.status).isEqualTo(OrderStatus.CREATED)

        // 3. The order was actually persisted
        verify(orderRepository).save(any())

        // 4. An OrderCreatedEvent was published with the persisted total
        val eventCaptor = argumentCaptor<OrderCreatedEvent>()
        verify(eventProducer).publishOrderCreated(eventCaptor.capture())
        assertThat(eventCaptor.firstValue.customerId).isEqualTo(customerId)
        assertThat(eventCaptor.firstValue.totalPrice).isEqualByComparingTo("700.00")
    }

    @Test
    fun `rejects order and never saves it when an item is unavailable`() {
        val request = CreateOrderRequest(
            customerId = customerId,
            restaurantId = restaurantId,
            items = listOf(OrderItemRequest(menuItemId, 1)),
            deliveryAddress = null
        )

        whenever(restaurantClient.validateItems(any())).thenReturn(
            ValidateItemsResponse(
                valid = false,
                validatedItems = emptyList(),
                unavailableItems = listOf(menuItemId)
            )
        )

        assertThatThrownBy { orderService.createOrder(request) }
            .isInstanceOf(InvalidOrderException::class.java)

        // The whole point of validating BEFORE saving: nothing should be persisted
        // or published once the Restaurant Service says the cart isn't valid.
        verify(orderRepository, never()).save(any())
        verify(eventProducer, never()).publishOrderCreated(any())
    }

    @Test
    fun `publishes status changed event with previous and new status`() {
        val existing = Order(
            customerId = customerId,
            restaurantId = restaurantId,
            status = OrderStatus.CREATED
        )
        val orderId = UUID.randomUUID()

        whenever(orderRepository.findById(orderId)).thenReturn(Optional.of(existing))
        whenever(orderRepository.save(any())).thenAnswer { it.arguments[0] as Order }

        val response = orderService.updateStatus(orderId, OrderStatus.CONFIRMED)

        assertThat(response.status).isEqualTo(OrderStatus.CONFIRMED)

        val eventCaptor = argumentCaptor<OrderStatusChangedEvent>()
        verify(eventProducer).publishStatusChanged(eventCaptor.capture())
        assertThat(eventCaptor.firstValue.previousStatus).isEqualTo(OrderStatus.CREATED)
        assertThat(eventCaptor.firstValue.newStatus).isEqualTo(OrderStatus.CONFIRMED)
    }

    @Test
    fun `throws IllegalStateException on illegal status transition and does not publish`() {
        val existing = Order(
            customerId = customerId,
            restaurantId = restaurantId,
            status = OrderStatus.CREATED
        )
        val orderId = UUID.randomUUID()
        whenever(orderRepository.findById(orderId)).thenReturn(Optional.of(existing))

        assertThatThrownBy { orderService.updateStatus(orderId, OrderStatus.DELIVERED) }
            .isInstanceOf(IllegalStateException::class.java)

        verify(orderRepository, never()).save(any())
        verify(eventProducer, never()).publishStatusChanged(any())
    }

    @Test
    fun `throws OrderNotFoundException when order does not exist`() {
        val orderId = UUID.randomUUID()
        whenever(orderRepository.findById(orderId)).thenReturn(Optional.empty())

        assertThatThrownBy { orderService.getOrder(orderId) }
            .isInstanceOf(OrderNotFoundException::class.java)
    }
}
