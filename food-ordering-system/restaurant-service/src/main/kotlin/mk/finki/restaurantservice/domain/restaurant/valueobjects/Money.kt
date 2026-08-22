package mk.finki.restaurantservice.domain.restaurant.valueobjects

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.math.BigDecimal

@Embeddable
data class Money(
    @Column(name = "amount")
    val amount: BigDecimal,
    @Column(name = "currency")
    val currency: String
) {
    init {
        require(amount >= BigDecimal.ZERO) { "Money amount cannot be negative" }
        require(currency.matches(Regex("[A-Z]{3}"))) { "Currency must be a 3-letter ISO code" }
    }

    fun add(delta: Money): Money {
        require(currency == delta.currency) { "Cannot add different currencies" }
        return Money(amount.add(delta.amount), currency)
    }

    fun multiply(quantity: Int): Money = Money(amount.multiply(BigDecimal(quantity)), currency)

    companion object {
        fun zero(currency: String = "USD") = Money(BigDecimal.ZERO, currency)
    }
}
