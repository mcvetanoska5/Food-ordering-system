# Restaurant & Catalog Service Exercise

### Overview
This exercise teaches the fundamentals of **Domain-Driven Design (DDD)** and microservice development using **Spring Boot 3**. By the end of this module, you will be able to implement a rich domain model with aggregate roots, expose business logic via REST APIs, integrate with PostgreSQL for persistence, and provide an AI-accessible interface via a Model Context Protocol (**MCP**) server.

---

### Core Concepts

#### 1. Aggregate and Aggregate Root
An **Aggregate** is a cluster of domain objects that can be treated as a single unit. The **Aggregate Root** is the only member of the aggregate that outside objects are allowed to hold a reference to. It ensures the integrity of the entire aggregate.

*   **Boundary**: Defines what's inside and outside the consistency zone.
*   **Invariants**: Business rules that must always remain true (e.g., "no duplicate menu item names").
*   **Identity**: Only the Root has a global identity; child entities have local identity.

```java
// Restaurant.java
@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    private UUID id;
    
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menu = new ArrayList<>();

    public void addMenuItem(String name, BigDecimal price) {
        // Enforce invariant: No duplicate names within a restaurant's menu
        boolean exists = menu.stream().anyMatch(item -> item.getName().equalsIgnoreCase(name));
        if (exists) {
            throw new IllegalArgumentException("Menu item already exists");
        }
        MenuItem newItem = new MenuItem(name, price, this);
        this.menu.add(newItem);
    }
}
```

The `Restaurant` class acts as the Root. It controls access to `MenuItem` objects, ensuring that any new item added follows the restaurant's business rules.

**Structure Diagram:**
```text
[ Restaurant (Root) ] --owns--> [ MenuItem (Child) ]
          |
          +--- name: String
          +--- active: boolean
          +--- menu: List<MenuItem>
```

#### 2. Repository Port and Adapter
In DDD, the domain layer defines **Ports** (interfaces) for persistence, while the infrastructure layer provides **Adapters** (implementations). This keeps the business logic decoupled from the database technology.

*   **Port**: A domain-defined interface (`RestaurantRepository`).
*   **Adapter**: A concrete implementation using JPA/PostgreSQL (`RestaurantRepositoryAdapter`).

```java
// RestaurantRepository.java (Domain Port)
public interface RestaurantRepository {
    Optional<Restaurant> findById(UUID id);
    Restaurant save(Restaurant restaurant);
}

// RestaurantRepositoryAdapter.java (Infrastructure Adapter)
@Component
@RequiredArgsConstructor
public class RestaurantRepositoryAdapter implements RestaurantRepository {
    private final RestaurantJpaRepository jpaRepository;

    @Override
    public Restaurant save(Restaurant restaurant) {
        return jpaRepository.save(restaurant);
    }
}
```

This separation allows you to switch from PostgreSQL to another database without changing a single line of business logic.

---

### The Framework/Tool

**Spring Boot 3** is the industry-standard framework for building Java microservices. It solves the problem of "boilerplate" configuration by providing **Convention over Configuration** and **Auto-configuration**.

#### Key Annotations

| Annotation | Location | Purpose |
| :--- | :--- | :--- |
| `@Entity` | Domain Class | Marks a class as a JPA entity mapped to a database table. |
| `@Table` | Domain Class | Specifies the name of the database table for the entity. |
| `@Id` | Field | Defines the primary key of the entity. |
| `@GeneratedValue`| Field | Configures the strategy for primary key generation. |
| `@OneToMany` | Field | Defines a one-to-many relationship (Restaurant -> MenuItems). |
| `@ManyToOne` | Field | Defines a many-to-one relationship (MenuItem -> Restaurant). |
| `@Enumerated` | Field | Specifies that an enum should be persisted as a String or Ordinal. |
| `@Service` | Application Layer | Marks a class as a business service (Component scanning). |
| `@Transactional` | Method/Service | Ensures a method runs within a database transaction context. |
| `@RestController`| Web Layer | Marks a class as a web controller handling REST requests. |
| `@RequestMapping`| Class/Method | Maps web requests to specific controller methods. |
| `@KafkaListener` | Messaging Layer | Marks a method to listen for messages on a Kafka topic. |
| `@Component` | Any Layer | Generic stereotype for any Spring-managed bean. |

#### Management & Gotchas
*   **Managed Automatically**: Bean lifecycle, transaction boundaries, connection pooling, and JSON serialization/deserialization.
*   **Developer Configuration**: Database credentials, Kafka brokers, and custom domain logic.
*   **Gotcha**: **Lombok + JPA**. JPA requires a no-args constructor for entities. Use `@NoArgsConstructor(access = AccessLevel.PROTECTED)` to satisfy JPA while preventing manual instantiation.
*   **Gotcha**: **Lazy Loading**. Accessing a `@OneToMany` collection outside a `@Transactional` session will throw a `LazyInitializationException`.

#### Request Flow
```text
HTTP Request
     |
     v
[ RestaurantController ] (Web Layer - DTO handling)
     |
     v
[ RestaurantApplicationService ] (Application Layer - Transaction/Orchestration)
     |
     v
[ Restaurant (Aggregate) ] (Domain Layer - Business Rules)
     |
     v
[ RestaurantRepository ] (Port - Domain Interface)
     |
     v
[ RestaurantRepositoryAdapter ] (Adapter - JPA Implementation)
     |
     v
[ PostgreSQL ] (Persistence)
```

---

### Project Setup

#### Scaffolding
Create the project via [Spring Initializr](https://start.spring.io/) with:
*   **Project**: Maven
*   **Language**: Java 17
*   **Spring Boot**: 3.3.2
*   **Dependencies**: Spring Web, Spring Data JPA, Spring for Apache Kafka, PostgreSQL Driver, Validation, Lombok, H2 Database.

#### Manual Dependencies
Add these to your `pom.xml`:
```xml
<!-- pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.2</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

#### Configuration
```yaml
# application.yml
spring:
  datasource:
    # Default to H2 for local development
    url: ${DB_URL:jdbc:h2:mem:restaurantdb}
    driver-class-name: ${DB_DRIVER:org.h2.Driver}
  jpa:
    hibernate:
      ddl-auto: update
  kafka:
    consumer:
      group-id: restaurant-service-group
      # Trust domain package for JSON deserialization
      properties:
        spring.json.trusted.packages: "mk.finki.restaurantservice.*"
```

---

### Step-by-Step Code Walkthrough: Adding a Menu Item

**Step 1: The Request (DTO)**
The client sends a `CreateMenuItemRequest` via HTTP POST.
```java
// RestaurantController.java
public record CreateMenuItemRequest(String name, BigDecimal price) {}
```

**Step 2: The Controller Entry Point**
The controller receives the UUID and DTO, then delegates to the application service.
```java
// RestaurantController.java
@PostMapping("/{id}/menu-items")
public void addMenuItem(@PathVariable UUID id, @RequestBody CreateMenuItemRequest request) {
    restaurantService.addMenuItem(id, request.name(), request.price());
}
```

**Step 3: Orchestration in the Application Service**
The service starts a transaction, fetches the aggregate root, and triggers the domain logic.
```java
// RestaurantApplicationService.java
@Transactional
public void addMenuItem(UUID restaurantId, String name, BigDecimal price) {
    Restaurant restaurant = getRestaurant(restaurantId); // Fetch Root
    restaurant.addMenuItem(name, price); // Domain logic
    restaurantRepository.save(restaurant); // Persist changes
}
```
*Design Decision*: The service doesn't check for duplicate names. It delegates this to the `Restaurant` aggregate to ensure the business rule is enforced regardless of which service calls it.

**Step 4: Domain Logic in the Aggregate Root**
The `Restaurant` ensures the menu remains valid according to business rules.
```java
// Restaurant.java
public void addMenuItem(String name, BigDecimal price) {
    if (menu.stream().anyMatch(i -> i.getName().equals(name))) {
        throw new IllegalArgumentException("Duplicate name");
    }
    this.menu.add(new MenuItem(name, price, this));
}
```
*Design Decision*: `MenuItem` constructor is package-private. This prevents other packages from creating "homeless" menu items that aren't attached to a `Restaurant`.

**Step 5: Persistence via the Adapter**
The JPA adapter saves the entire aggregate state to PostgreSQL.

---

### MCP Server

МК (изворно барање): Како дополнителна задолжителна функционалност, секој проект треба да содржи MCP сервер. MCP (Model Context Protocol) е отворен стандард за поврзување на AI апликации со надворешни системи, алатки и извори на податоци. Во рамки на проектот, MCP серверот треба да овозможи пристап до релевантни функционалности или податоци од системот. Начинот на интеграција е по избор на тимот, но треба да биде функционален и јасно објаснет.

English paraphrase: Every project must additionally ship an MCP (Model Context Protocol) server — an open standard for connecting AI applications to external systems, tools, and data sources. The MCP server must expose meaningful functionality or data from the project's own system. The team chooses the integration approach, but it must be working and clearly documented.

#### What the MCP server exposes
The Restaurant MCP Server exposes the following read-only tools and resources to AI clients:
*   **`list_restaurants`**: Returns a list of all restaurants in the catalog (ID and Name).
*   **`get_restaurant_menu`**: Takes a `restaurantId` and returns the full menu, including item names, prices, and availability status.
*   **`check_menu_item_availability`**: A specialized tool to verify if specific items are currently `AVAILABLE`.

*Note*: Mutations (creating restaurants or adding items) are **excluded** from the MCP server to ensure data integrity and security, remaining exclusive to the authenticated REST API.

#### How it communicates with the rest of the system
The MCP server acts as a **thin adapter** sitting alongside the REST controllers. It calls the same `RestaurantApplicationService` methods, ensuring that the AI client sees the exact same data as the web UI.

```text
[ AI Client ] -> [ MCP Server (stdio/SSE) ] -> [ RestaurantApplicationService ] -> [ Domain ]
```

#### How it was tested
Integration was verified using a **terminal-based MCP inspector**. A sample interaction follows:
*   *AI Query*: "What's on the menu at restaurant `550e8400-e29b-41d4-a716-446655440000`?"
*   *Tool Call*: `get_restaurant_menu(restaurantId: "550e8400...")`
*   *Response*: `{"restaurantName": "Pizza Place", "menu": [{"name": "Margherita", "price": 10.99, "status": "AVAILABLE"}]}`

#### Role in the overall architecture
The MCP server serves as the **AI-native entry point** to the Restaurant & Catalog Service. By reusing existing Application Services and DTOs, it avoids logic duplication and ensures that business rules (like availability checks) are consistent across all interfaces, whether used by a human via a browser or an AI agent.
