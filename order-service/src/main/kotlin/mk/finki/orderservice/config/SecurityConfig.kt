package mk.finki.orderservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

/**
 * Validates the same Keycloak JWT the gateway already checked (defense in depth —
 * this service must never trust that a request only ever arrives via the gateway).
 * Realm roles from Keycloak ("customer", "restaurant_owner") are mapped to
 * Spring Security authorities prefixed with ROLE_.
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    // MCP endpoint (JSON-RPC over Streamable HTTP) and its plain-REST
                    // wrapper are meant to be called directly by AI/MCP clients that
                    // don't carry a Keycloak-issued JWT, so they're excluded from auth.
                    .requestMatchers("/mcp/**", "/mcp").permitAll()
                    .requestMatchers("/api/orders/place", "/api/orders/*/summary").permitAll()
                    .anyRequest().hasAnyRole("customer", "restaurant_owner")
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { it.jwtAuthenticationConverter(jwtAuthConverter()) }
            }
        return http.build()
    }

    /**
     * Keycloak nests realm roles under claim "realm_access": { "roles": [...] },
     * which Spring's built-in JwtGrantedAuthoritiesConverter can't read directly
     * (it only looks at a flat top-level claim), so we extract it manually.
     */
    private fun jwtAuthConverter(): JwtAuthenticationConverter {
        val realmRolesConverter = Converter<Jwt, Collection<GrantedAuthority>> { jwt ->
            @Suppress("UNCHECKED_CAST")
            val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
            val roles = realmAccess?.get("roles") as? List<String> ?: emptyList()
            roles.map { SimpleGrantedAuthority("ROLE_$it") }
        }

        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter(realmRolesConverter)
        return converter
    }
}