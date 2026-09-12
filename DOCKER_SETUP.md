# Docker Orchestration Setup - Quick Reference

## ✅ COMPLETED ITEMS

### 1. Dockerfiles Created
- ✅ `/order-service/Dockerfile` — Already existed, confirmed correct
- ✅ `/restaurant-service/Dockerfile` — Created (multi-stage build, Alpine JRE 17)

### 2. Docker Compose Updated
- ✅ `/docker-compose.yml` — Updated to include:
  - All infrastructure (Postgres × 2, Kafka, Kafka-UI, Consul)
  - Both app services with proper build contexts
  - Shared `shared_net` bridge network
  - Healthchecks for all services
  - Proper `depends_on` with `condition: service_healthy`

### 3. Application Configuration Updated
- ✅ `order-service/src/main/resources/application.yml`
  - `spring.datasource.url`: localhost:5433 → postgres-order:5432
  - `spring.cloud.consul.host`: localhost → consul
  - `spring.kafka.bootstrap-servers`: localhost:9092 → kafka:9092
  - Consumer bootstrap-servers: localhost:9092 → kafka:9092

- ✅ `restaurant-service/src/main/resources/application.yml`
  - `spring.datasource.url`: localhost:5432 → postgres-restaurant:5432
  - `spring.cloud.consul.host`: localhost → consul
  - `spring.kafka.bootstrap-servers`: localhost:9092 → kafka:9092
  - Consumer bootstrap-servers: localhost:9092 → kafka:9092

---

## 🚀 STARTUP COMMANDS

### Full Stack (Recommended)
```bash
cd /path/to/food-ordering-system
docker compose up --build
```

### Cleanup (if needed)
```bash
docker compose down -v  # -v removes named volumes (PostgreSQL data)
```

### View Logs
```bash
docker compose logs -f order-service      # Order service only
docker compose logs -f restaurant-service # Restaurant service only
docker compose logs -f kafka              # Kafka only
docker compose logs                       # All services
```

---

## 🌐 SWAGGER URLs (after both services report "Started")

### Order Service
```
http://localhost:8081/swagger-ui/index.html
```

### Restaurant Service
```
http://localhost:8082/swagger-ui/index.html
```

### Kafka UI (for debugging topics/messages)
```
http://localhost:8085
```

### Consul (Service Discovery)
```
http://localhost:8500/ui/
```

---

## 📋 LOG PATTERNS TO WATCH FOR

### Service Startup Success
Watch for these logs in order:

1. **Infrastructure Startup** (first 10-20 seconds)
   ```
   postgres-order_1 | ... database system is ready to accept connections
   postgres-restaurant_1 | ... database system is ready to accept connections
   kafka_1 | [KafkaServer id=0] started
   consul_1 | ==> Listening for HTTP requests on
   ```

2. **Order Service Startup** (should appear 10-30 seconds in)
   ```
   order-service | ... Tomcat started on port(s): 8081
   order-service | ... Started OrderServiceApplication
   order-service | ... Registering service with Consul
   order-service | ... [ConsulDiscoveryClient] Local service registered with Consul
   ```

3. **Restaurant Service Startup** (should appear 10-30 seconds in)
   ```
   restaurant-service | ... Tomcat started on port(s): 8082
   restaurant-service | ... Started RestaurantServiceApplication
   restaurant-service | ... Registering service with Consul
   restaurant-service | ... [ConsulDiscoveryClient] Local service registered with Consul
   ```

### Kafka Connection Success
Look for (both services):
```
... Initializing Spring Kafka with Spring BootKafkaListenerEndpointRegistry
... Kafka listener container started
... Consumer group subscribed to topic(s): [menu.item.availability.changed]  (order-service)
... Consumer group subscribed to topic(s): [order.placed]  (restaurant-service)
```

### Postgres Connection Success
Look for (both services):
```
... HHH000204: Processing PersistenceUnitMetadata [name: default]
... Database product name: PostgreSQL
... Successfully retrieved schema metadata
```

### Consul Registration Success
Look for (both services):
```
... ServiceDiscoveryClient: Registering service with Consul
... [ConsulDiscoveryClient] Local service registered with Consul
```

---

## ⚠️ DOCKER-SPECIFIC NETWORKING NOTES

### Why Service Names Instead of localhost?
- Inside Docker, containers communicate via service names on the internal bridge network
- `postgres-order:5432` resolves to the postgres-order container's IP via Docker DNS
- `localhost:5432` would resolve to the container itself, which doesn't have PostgreSQL

### All Service Names (for reference)
- `postgres-order` — Order service database (internal port 5432 → external 5433)
- `postgres-restaurant` — Restaurant service database (internal port 5432 → external 5432)
- `kafka` — Kafka broker (internal + external 9092)
- `kafka-ui` — Kafka UI (internal 8080 → external 8085)
- `consul` — Consul discovery (internal 8500 → external 8500)
- `order-service` — Order app (internal 8081 → external 8081)
- `restaurant-service` — Restaurant app (internal 8082 → external 8082)

---

## 🔍 HEALTH CHECK SUMMARY

All services have healthchecks configured:
- **Postgres**: `pg_isready` check
- **Kafka**: Broker API version check
- **Consul**: Leader check
- **Apps**: Spring Boot actuator `/actuator/health` endpoint

The app services will only start after infrastructure passes health checks
(`depends_on: condition: service_healthy`).

---

## 📝 TROUBLESHOOTING

### "Connection refused" errors?
- Ensure all services are healthy: `docker compose ps`
- Check container logs: `docker compose logs <service-name>`
- Verify network: `docker network ls` and `docker network inspect shared_net`

### Apps won't start, showing database connection errors?
- Check Postgres is healthy: `docker compose logs postgres-order`
- Verify app service has correct env vars: `docker inspect order-service | grep SPRING`

### Kafka not receiving messages?
- Verify Kafka is healthy: `docker compose logs kafka`
- Check Kafka-UI at http://localhost:8085 to see topics/messages

### Consul not showing services?
- Check Consul dashboard: http://localhost:8500/ui/
- Verify apps are registering: look for "Registered with Consul" in logs

---

## 🎯 NEXT STEPS

1. Run: `docker compose up --build`
2. Wait for both services to show "Started ...Application" (2-3 minutes)
3. Open Swagger UIs above
4. Test REST endpoints
5. Monitor Kafka-UI for event messages between services
