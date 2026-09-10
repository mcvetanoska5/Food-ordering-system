package mk.finki.orderservice.domain.order.valueobjects

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class CustomerId @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(
    @Column(name = "customer_id")
    @get:JsonValue
    @Schema(type = "string", format = "uuid")
    override val value: UUID
) : Serializable, Identifier<UUID> {
    init {
        require(value.toString().isNotEmpty()) { "CustomerId cannot be empty" }
    }
}
