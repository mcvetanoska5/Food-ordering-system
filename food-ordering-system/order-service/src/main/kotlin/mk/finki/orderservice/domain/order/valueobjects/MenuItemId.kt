package mk.finki.orderservice.domain.order.valueobjects

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable
import java.util.UUID

@Embeddable
data class MenuItemId(
    @Column(name = "menu_item_id")
    override val value: UUID
) : Serializable, Identifier<UUID> {
    init {
        require(value.toString().isNotEmpty()) { "MenuItemId cannot be empty" }
    }
}
