package com.eridanimelo.user_api.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.eridanimelo.user_api.config.exception.KeycloakServiceException;
import com.eridanimelo.user_api.config.exception.UserNotFoundException;
import com.eridanimelo.user_api.service.KeycloakService;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.server.url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.id}")
    private String adminClientId;

    @Value("${keycloak.client.secret}")
    private String adminClientSecret;

    @PostConstruct
    private void validateProperties() {
        Objects.requireNonNull(keycloakServerUrl, "The property 'keycloak.server.url' must not be null");
        Objects.requireNonNull(realm, "The property 'keycloak.realm' must not be null");
        Objects.requireNonNull(adminClientId, "The property 'keycloak.client.id' must not be null");
        Objects.requireNonNull(adminClientSecret, "The property 'keycloak.client.secret' must not be null");
    }

    private String getAdminAccessToken() {
        String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakServerUrl, realm);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", adminClientId);
        body.add("client_secret", adminClientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().get("access_token").toString();
        } else {
            throw new KeycloakServiceException("Failed to obtain access token");
        }
    }

    @Override
    public void createUser(String username, String firstName, String lastName, String email, String password) {
        String accessToken = getAdminAccessToken();

        String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);

        Map<String, Object> credentials = Map.of(
                "type", "password",
                "value", password,
                "temporary", false);

        Map<String, Object> requestBody = Map.of(
                "username", username,
                "enabled", true,
                "email", email,
                "firstName", firstName,
                "lastName", lastName,
                "credentials", Collections.singletonList(credentials));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new KeycloakServiceException("Failed to create user: " + response.getBody());
        }
    }

    @Override
    public String findUserIdByEmail(String email) {
        String accessToken = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users?email=%s", keycloakServerUrl, realm, email);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null && !response.getBody().isEmpty()) {
            Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
            return user.get("id").toString();
        }

        throw new UserNotFoundException("User not found with email: " + email);
    }

    @Override
    public void addUserRole(String userId, String roleName) {
        String accessToken = getAdminAccessToken();
        String roleUrl = String.format("%s/admin/realms/%s/roles/%s", keycloakServerUrl, realm, roleName);
        String assignRoleUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakServerUrl,
                realm, userId);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // Obter detalhes da role
        ResponseEntity<Map> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers),
                Map.class);
        if (roleResponse.getStatusCode() != HttpStatus.OK || roleResponse.getBody() == null) {
            throw new KeycloakServiceException("Role not found or invalid response: " + roleName);
        }
        Map<String, Object> role = roleResponse.getBody();

        // Adicionar role ao usuário
        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(Collections.singletonList(role), headers);
        ResponseEntity<Void> assignResponse = restTemplate.postForEntity(assignRoleUrl, entity, Void.class);

        if (!assignResponse.getStatusCode().is2xxSuccessful()) {
            throw new KeycloakServiceException("Failed to assign role to user: " + assignResponse.getStatusCode());
        }
    }

    @Override
    public void deleteUser(String email) {
        String userId = findUserIdByEmail(email);
        String accessToken = getAdminAccessToken();

        String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers),
                Void.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new KeycloakServiceException("Failed to delete user with email: " + email);
        }
    }

    @Override
    public void disableUser(String email) {
        String userId = findUserIdByEmail(email);
        String accessToken = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);

        String requestBody = "{ \"enabled\": false }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new KeycloakServiceException(
                    "Failed to disable user: " + response.getStatusCode() + " " + response.getBody());
        }
    }

    @Override
    public void enableUser(String email) {
        String userId = findUserIdByEmail(email);
        String accessToken = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);

        String requestBody = "{ \"enabled\": true }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new KeycloakServiceException(
                    "Failed to enable user: " + response.getStatusCode() + " " + response.getBody());
        }
    }

    @Override
    public List<Map<String, Object>> listAllUsers() {
        String accessToken = getAdminAccessToken();
        String usersUrl = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Buscar todos os usuários
        ResponseEntity<List> response = restTemplate.exchange(usersUrl, HttpMethod.GET, entity, List.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            List<Map<String, Object>> usersWithRoles = new ArrayList<>();
            List<Map<String, Object>> users = response.getBody();

            for (Map<String, Object> user : users) {
                String userId = (String) user.get("id");
                String rolesUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakServerUrl,
                        realm, userId);

                // Buscar roles para cada usuário
                ResponseEntity<List> rolesResponse = restTemplate.exchange(rolesUrl, HttpMethod.GET, entity,
                        List.class);
                List<Map<String, Object>> roles = rolesResponse.getStatusCode() == HttpStatus.OK
                        && rolesResponse.getBody() != null
                                ? rolesResponse.getBody()
                                : new ArrayList<>();

                // Adicionar os detalhes das roles ao usuário
                user.put("roles", roles);
                usersWithRoles.add(user);
            }

            return usersWithRoles;
        }

        throw new KeycloakServiceException("Failed to fetch users with roles");
    }

    @Override
    public void resetUserPassword(String email, String newPassword) {
        String userId = findUserIdByEmail(email);
        String accessToken = getAdminAccessToken();

        String url = String.format("%s/admin/realms/%s/users/%s/reset-password", keycloakServerUrl, realm, userId);

        Map<String, Object> requestBody = Map.of(
                "type", "password",
                "value", newPassword,
                "temporary", false);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new KeycloakServiceException("Failed to reset password for user: " + email);
        }
    }

    @Override
    public void removeUserRole(String userId, String roleName) {
        String accessToken = getAdminAccessToken();
        String roleUrl = String.format("%s/admin/realms/%s/roles/%s", keycloakServerUrl, realm, roleName);
        String removeRoleUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakServerUrl,
                realm, userId);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers),
                Map.class);

        if (roleResponse.getStatusCode() != HttpStatus.OK) {
            throw new KeycloakServiceException("Role not found: " + roleName);
        }

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId),
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);

        if (userResponse.getStatusCode() != HttpStatus.OK) {
            throw new KeycloakServiceException("User not found with ID: " + userId);
        }

        Map<String, Object> role = roleResponse.getBody();

        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(Collections.singletonList(role), headers);
        ResponseEntity<Void> removeResponse = restTemplate.exchange(removeRoleUrl, HttpMethod.DELETE, entity,
                Void.class);

        if (!removeResponse.getStatusCode().is2xxSuccessful()) {
            throw new KeycloakServiceException("Failed to remove role from user");
        }
    }

    @Override
    public List<Map<String, Object>> listAllRoles() {
        String accessToken = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/roles", keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new KeycloakServiceException("Unexpected response status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new KeycloakServiceException("Failed to fetch roles: " + e.getMessage(), e);
        }
    }
}
