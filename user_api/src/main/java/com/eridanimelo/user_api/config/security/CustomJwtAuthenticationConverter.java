package com.eridanimelo.user_api.config.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, JwtAuthenticationToken> {

    @Override
    public JwtAuthenticationToken convert(@NonNull Jwt jwt) {
        // Converte as authorities padrão do JWT
        JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = defaultGrantedAuthoritiesConverter.convert(jwt);

        // Adiciona lógica personalizada para mapear authorities adicionais, se
        // necessário
        Collection<GrantedAuthority> customAuthorities = mapCustomAuthorities(jwt);
        if (customAuthorities != null) {
            authorities.addAll(customAuthorities);
        }

        // Definir o principalName como um atributo customizado, como
        // "preferred_username"
        String principalName = jwt.getClaim("preferred_username");
        if (principalName == null) {
            principalName = jwt.getSubject(); // Fallback para o ID padrão (sub)
        }

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> mapCustomAuthorities(Jwt jwt) {
        // Implemente lógica para mapear authorities customizadas a partir do JWT
        // Exemplo: extraindo roles de um claim chamado "roles"
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            return roles.stream()
                    .map(role -> (GrantedAuthority) () -> "ROLE_" + role.toUpperCase()) // Prefixo padrão de ROLE_
                    .toList();
        }
        return List.of(); // Sem roles customizadas
    }
}
