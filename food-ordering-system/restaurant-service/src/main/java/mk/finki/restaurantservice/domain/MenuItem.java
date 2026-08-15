package mk.finki.restaurantservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MenuItem {
    @Id
    private UUID id;

    private String name;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private MenuItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    // Package-private constructor for the Restaurant aggregate root to use
    MenuItem(String name, BigDecimal price, Restaurant restaurant) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.status = MenuItemStatus.AVAILABLE;
        this.restaurant = restaurant;
    }

    public void updateStatus(MenuItemStatus status) {
        this.status = status;
    }
}
