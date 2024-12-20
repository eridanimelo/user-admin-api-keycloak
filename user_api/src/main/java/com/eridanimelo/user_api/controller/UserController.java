package com.eridanimelo.user_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.eridanimelo.user_api.dto.UserDTO;
import com.eridanimelo.user_api.service.KeycloakService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users in Keycloak")
public class UserController {

        private final KeycloakService keycloakService;

        @PostMapping("/create")
        @Operation(summary = "Create a new user", description = "Creates a new user in Keycloak with the provided details (username, first name, last name, email, and password).", responses = {
                        @ApiResponse(responseCode = "201", description = "User created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void createUser(@RequestBody UserDTO userDTO) {
                keycloakService.createUser(
                                userDTO.getUsername(),
                                userDTO.getFirstName(),
                                userDTO.getLastName(),
                                userDTO.getEmail(),
                                userDTO.getPassword());
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset user password", description = "Resets the password for the user identified by their email address in Keycloak. The new password must be provided.", responses = {
                        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void resetPassword(@RequestBody UserDTO userDTO) {
                keycloakService.resetUserPassword(userDTO.getEmail(), userDTO.getPassword());
        }

        @DeleteMapping("/delete")
        @Operation(summary = "Delete a user", description = "Deletes the user identified by their email address from Keycloak.", responses = {
                        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void deleteUser(@RequestParam String email) {
                keycloakService.deleteUser(email);
        }

        @PutMapping("/disable")
        @Operation(summary = "Disable a user", description = "Disables the user identified by their email address in Keycloak, preventing further login attempts.", responses = {
                        @ApiResponse(responseCode = "200", description = "User disabled successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void disableUser(@RequestParam String email) {
                keycloakService.disableUser(email);
        }

        @PutMapping("/enable")
        @Operation(summary = "Enable a user", description = "Enable the user identified by their email address in Keycloak, preventing further login attempts.", responses = {
                        @ApiResponse(responseCode = "200", description = "User Enable successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void enableUser(@RequestParam String email) {
                keycloakService.enableUser(email);
        }

        @GetMapping("/list")
        @Operation(summary = "List all users", description = "Retrieves a list of all users in the Keycloak realm.", responses = {
                        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public List<Map<String, Object>> listAllUsers() {
                return keycloakService.listAllUsers();
        }

        @GetMapping("/roles")
        @Operation(summary = "List all roles", description = "Retrieves a list of all roles in the Keycloak realm.", responses = {
                        @ApiResponse(responseCode = "200", description = "List of roles retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public List<Map<String, Object>> listAllRoles() {
                return keycloakService.listAllRoles();
        }

        @PostMapping("/users/{userId}/roles/add")
        @Operation(summary = "Assign a role to a user", description = "Assigns a specified role to the user identified by userId in Keycloak.", responses = {
                        @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "User or role not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void addUserRole(
                        @PathVariable String userId,
                        @RequestParam String roleName) {
                keycloakService.addUserRole(userId, roleName);
        }

        @PostMapping("/users/{userId}/roles/remove")
        @Operation(summary = "Remove a role to a user", description = "Remove a specified role to the user identified by userId in Keycloak.", responses = {
                        @ApiResponse(responseCode = "200", description = "Role remove successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "User or role not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void removeUserRole(
                        @PathVariable String userId,
                        @RequestParam String roleName) {
                keycloakService.removeUserRole(userId, roleName);
        }

}
