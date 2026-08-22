package mk.finki.orderservice.domain.order.valueobjects

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class CustomerId(
    @Column(name = "customer_id")
    val value: UUID
) : Serializable {
    init {
        require(value.toString().isNotEmpty()) { "CustomerId cannot be empty" }
    }
}
