package com.eridanimelo.user_api.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Desabilita CSRF para simplificar comunicação com o frontend (recomendado para
                // APIs RESTful)
                .csrf(csrf -> csrf.disable())

                // Configuração de autorização
                .authorizeHttpRequests(auth -> auth
                        // Permitir acesso público a endpoints específicos
                        .requestMatchers(
                                "/v3/api-docs/**", // Documentação do Swagger
                                "/swagger-ui/**", // Interface do Swagger UI
                                "/swagger-ui.html" // Página principal do Swagger
                        ).permitAll()
                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated())

                // Configuração do OAuth2 como servidor de recursos
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                // Configura o conversor de autenticação JWT (opcional)
                                .jwtAuthenticationConverter(
                                        new CustomJwtAuthenticationConverter())));

        return http.build();
    }

}
