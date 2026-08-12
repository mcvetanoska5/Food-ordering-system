package mk.finki.orderservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.openfeign.EnableFeignClients

/**
 * Order Service
 * Bounded context: Cart, Order lifecycle, Order status.
 * Owns its own PostgreSQL schema, separate from Restaurant & Catalog Service.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
class OrderServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderServiceApplication>(*args)
}
