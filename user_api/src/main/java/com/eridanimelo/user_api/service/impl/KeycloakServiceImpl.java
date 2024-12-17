package com.eridanimelo.user_api.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.eridanimelo.user_api.config.exception.UserNotFoundException;
import com.eridanimelo.user_api.service.KeycloakService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            throw new RuntimeException("Failed to obtain access token");
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
            throw new RuntimeException("Failed to create user: " + response.getBody());
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

        // Busca informações da role
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> roleResponse = restTemplate.exchange(roleUrl, HttpMethod.GET, new HttpEntity<>(headers),
                Map.class);

        if (roleResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Role not found: " + roleName);
        }

        Map<String, Object> role = roleResponse.getBody();

        // Adiciona a role ao usuário
        HttpEntity<List<Map<String, Object>>> entity = new HttpEntity<>(Collections.singletonList(role), headers);
        ResponseEntity<Void> assignResponse = restTemplate.postForEntity(assignRoleUrl, entity, Void.class);

        if (!assignResponse.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to assign role to user");
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
            throw new RuntimeException("Failed to delete user with email: " + email);
        }
    }

    @Override
    public void disableUser(String email) {
        // Reutiliza o método findUserIdByEmail para obter o userId
        String userId = findUserIdByEmail(email);

        String accessToken = getAdminAccessToken();

        // URL para desabilitar o usuário
        String url = String.format("%s/admin/realms/%s/users/%s", keycloakServerUrl, realm, userId);

        // Corpo da requisição para desabilitar o usuário
        String requestBody = "{ \"enabled\": false }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        // Verifica se a resposta foi bem-sucedida
        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException(
                    "Failed to disable user: " + response.getStatusCode() + " " + response.getBody());
        }

    }

    @Override
    public List<Map<String, Object>> listAllUsers() {
        String accessToken = getAdminAccessToken();
        String url = String.format("%s/admin/realms/%s/users", keycloakServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to fetch users");
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
            throw new RuntimeException("Failed to reset password for user: " + email);
        }
    }
}
