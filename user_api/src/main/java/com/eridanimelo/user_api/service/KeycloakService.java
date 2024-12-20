package com.eridanimelo.user_api.service;

import java.util.List;
import java.util.Map;

public interface KeycloakService {
    void createUser(String username, String firstName, String lastName, String email, String password);

    void addUserRole(String userId, String roleName);

    String findUserIdByEmail(String email);

    void deleteUser(String email);

    void disableUser(String email);

    void enableUser(String email);

    List<Map<String, Object>> listAllUsers();

    void resetUserPassword(String email, String newPassword);

    void removeUserRole(String userId, String roleName);

    List<Map<String, Object>> listAllRoles();

}
