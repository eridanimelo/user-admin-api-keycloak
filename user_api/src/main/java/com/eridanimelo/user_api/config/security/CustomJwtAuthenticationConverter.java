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
        // Converts the default JWT authorities
        JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        Collection<GrantedAuthority> authorities = defaultGrantedAuthoritiesConverter.convert(jwt);

        // Adds custom logic to map additional authorities, if necessary
        Collection<GrantedAuthority> customAuthorities = mapCustomAuthorities(jwt);
        if (customAuthorities != null) {
            authorities.addAll(customAuthorities);
        }

        // Set the principalName as a custom attribute, such as "preferred_username"
        String principalName = jwt.getClaim("preferred_username");
        if (principalName == null) {
            principalName = jwt.getSubject(); // Fallback to the default ID (sub)
        }

        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> mapCustomAuthorities(Jwt jwt) {
        // Implement logic to map custom authorities from the JWT
        // Example: extracting roles from a claim called "roles"
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            return roles.stream()
                    .map(role -> (GrantedAuthority) () -> "ROLE_" + role.toUpperCase()) // Default ROLE_ prefix
                    .toList();
        }
        return List.of(); // No custom roles
    }
}
