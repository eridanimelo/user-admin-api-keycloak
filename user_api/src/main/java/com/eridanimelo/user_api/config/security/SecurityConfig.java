package com.eridanimelo.user_api.config.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
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
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("*")); // Permite todas as origens
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Permite todos os
                                                                                                     // métodos
                configuration.setAllowedHeaders(List.of("*")); // Permite todos os cabeçalhos
                configuration.setExposedHeaders(List.of("Authorization")); // Expor cabeçalhos específicos
                configuration.setAllowCredentials(false); // Se não for necessário usar cookies ou credenciais

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration); // Aplica a configuração a todos os endpoints
                return source;
        }

}
