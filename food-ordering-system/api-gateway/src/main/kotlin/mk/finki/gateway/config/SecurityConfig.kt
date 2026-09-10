package mk.finki.gateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        http
            .csrf { it.disable() }
            .authorizeExchange { exchange ->
                exchange
                    .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                    .pathMatchers("/api/restaurants/**", "/api/menu-items/**").hasRole("RESTAURANT_OWNER")
                    .pathMatchers("/api/orders/**").hasRole("CUSTOMER")
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
        return http.build()
    }
}
