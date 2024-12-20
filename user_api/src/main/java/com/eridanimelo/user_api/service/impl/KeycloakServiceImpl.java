package com.eridanimelo.user_api.service.impl;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eridanimelo.user_api.service.KeycloakService;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${keycloak.server.url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client.id}")
    private String clientId;

    @Value("${keycloak.client.secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    private Keycloak keycloak;

    @PostConstruct
    public void init() {
        try {
            this.keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(adminUsername)
                    .password(adminPassword)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Keycloak client", e);
        }
    }

    @Override
    public String createUser(String username, String firstName, String lastName, String email, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setRequiredActions(Collections.singletonList("VERIFY_EMAIL"));

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() == 201) {
            String userId = response.getLocation().getPath().replaceAll(".*/", "");
            setUserPassword(userId, password);

            // Enviar e-mail de verificação manualmente, se necessário
            UserResource userResource = keycloak.realm(realm).users().get(userId);
            userResource.sendVerifyEmail();
            return userId;
        } else {
            throw new RuntimeException("Failed to create user: " + response.getStatus());
        }
    }

    private void setUserPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        keycloak.realm(realm).users().get(userId).resetPassword(credential);
    }

    @Override
    public void disableUser(String userId) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        user.setEnabled(false);
        keycloak.realm(realm).users().get(userId).update(user);
    }

    @Override
    public void enableUser(String userId) {
        UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
        user.setEnabled(true);
        keycloak.realm(realm).users().get(userId).update(user);
    }

    @Override
    public void assignRoleToUser(String userId, String roleName) {
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().add(Collections.singletonList(role));
    }

    @Override
    public Optional<UserRepresentation> findUserByEmail(String email) {
        List<UserRepresentation> users = keycloak.realm(realm).users().search(email, 0, 1);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public void deleteUser(String userId) {
        keycloak.realm(realm).users().get(userId).remove();
    }

    @Override
    public void resetUserPassword(String userId, String temporaryPassword) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(temporaryPassword);
        credential.setTemporary(true);

        keycloak.realm(realm).users().get(userId).resetPassword(credential);
    }

    @Override
    public List<UserRepresentation> listAllUsers() {
        List<UserRepresentation> users = keycloak.realm(realm).users().list();

        for (UserRepresentation user : users) {
            List<RoleRepresentation> roles = keycloak.realm(realm)
                    .users()
                    .get(user.getId())
                    .roles()
                    .realmLevel()
                    .listEffective();

            List<String> filteredRoles = roles.stream()
                    .map(RoleRepresentation::getName)
                    .filter(roleName -> !roleName.equals("uma_authorization") &&
                            !roleName.equals("offline_access") &&
                            !roleName.equals("default-roles-user-api"))
                    .collect(Collectors.toList());

            user.setAttributes(Map.of("roles", filteredRoles));
        }

        return users;
    }

    @Override
    public void removeUserRole(String userId, String roleName) {
        RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
        keycloak.realm(realm).users().get(userId).roles().realmLevel().remove(Collections.singletonList(role));
    }

    @Override
    public List<RoleRepresentation> listAllRoles() {
        List<RoleRepresentation> allRoles = keycloak.realm(realm).roles().list();

        // Filtro para remover roles específicas
        return allRoles.stream()
                .filter(role -> !role.getName().equals("uma_authorization") &&
                        !role.getName().equals("offline_access") &&
                        !role.getName().equals("default-roles-user-api"))
                .collect(Collectors.toList());
    }

}
