package mk.finki.restaurantservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().hasAnyRole("customer", "restaurant_owner")
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { it.jwtAuthenticationConverter(jwtAuthConverter()) }
            }
        return http.build()
    }

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
