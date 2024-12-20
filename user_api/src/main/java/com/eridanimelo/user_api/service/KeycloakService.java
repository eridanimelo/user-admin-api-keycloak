package com.eridanimelo.user_api.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakService {
    String createUser(String username, String firstName, String lastName, String email, String password);

    void assignRoleToUser(String userId, String roleName);

    Optional<UserRepresentation> findUserByEmail(String email);

    void deleteUser(String email);

    void disableUser(String email);

    void enableUser(String email);

    List<UserRepresentation> listAllUsers();

    void resetUserPassword(String email, String newPassword);

    void removeUserRole(String userId, String roleName);

    List<RoleRepresentation> listAllRoles();

}
