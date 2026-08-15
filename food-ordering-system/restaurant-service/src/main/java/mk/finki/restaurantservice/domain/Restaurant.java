package mk.finki.restaurantservice.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Restaurant {
    @Id
    private UUID id;

    private String name;
    private String address;
    private boolean active;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menu = new ArrayList<>();

    public void addMenuItem(String name, BigDecimal price) {
        // Enforce business rule: No duplicate names within a restaurant's menu
        boolean exists = menu.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name));
        if (exists) {
            throw new IllegalArgumentException("Menu item with name " + name + " already exists");
        }
        MenuItem newItem = new MenuItem(name, price, this);
        this.menu.add(newItem);
    }
}
