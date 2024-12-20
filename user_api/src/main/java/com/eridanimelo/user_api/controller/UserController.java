package com.eridanimelo.user_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.eridanimelo.user_api.dto.UserRequestDTO;
import com.eridanimelo.user_api.service.KeycloakService;

import java.util.List;
import java.util.Optional;

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
        public void createUser(@RequestBody UserRequestDTO userRequestDTO) {
                UserRepresentation user = userRequestDTO.getUser();
                String password = userRequestDTO.getPassword();

                keycloakService.createUser(
                                user.getUsername(),
                                user.getFirstName(),
                                user.getLastName(),
                                user.getEmail(),
                                password);
        }

        @PostMapping("/reset-password")
        @Operation(summary = "Reset user password", description = "Resets the password for the user identified by their email address in Keycloak. The new password must be provided.", responses = {
                        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void resetPassword(@RequestBody UserRequestDTO userRequestDTO) {
                String email = userRequestDTO.getUser().getEmail();
                String password = userRequestDTO.getPassword();

                Optional<UserRepresentation> userOptional = keycloakService.findUserByEmail(email);
                if (userOptional.isPresent()) {
                        String userId = userOptional.get().getId();
                        keycloakService.resetUserPassword(userId, password);
                } else {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                }
        }

        @DeleteMapping("/delete")
        @Operation(summary = "Delete a user", description = "Deletes the user identified by their userId from Keycloak.", responses = {
                        @ApiResponse(responseCode = "200", description = "User deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void deleteUser(@RequestParam String userId) {
                keycloakService.deleteUser(userId);
        }

        @PutMapping("/disable")
        @Operation(summary = "Disable a user", description = "Disables the user identified by their userId in Keycloak, preventing further login attempts.", responses = {
                        @ApiResponse(responseCode = "200", description = "User disabled successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void disableUser(@RequestParam String userId) {
                keycloakService.disableUser(userId);
        }

        @PutMapping("/enable")
        @Operation(summary = "Enable a user", description = "Enable the user identified by their userId address in Keycloak, preventing further login attempts.", responses = {
                        @ApiResponse(responseCode = "200", description = "User Enable successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void enableUser(@RequestParam String userId) {
                keycloakService.enableUser(userId);
        }

        @GetMapping("/list")
        @Operation(summary = "List all users", description = "Retrieves a list of all users in the Keycloak realm.", responses = {
                        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public List<UserRepresentation> listAllUsers() {
                return keycloakService.listAllUsers();
        }

        @GetMapping("/roles")
        @Operation(summary = "List all roles", description = "Retrieves a list of all roles in the Keycloak realm.", responses = {
                        @ApiResponse(responseCode = "200", description = "List of roles retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public List<RoleRepresentation> listAllRoles() {
                return keycloakService.listAllRoles();
        }

        @PostMapping("/{userId}/roles/add")
        @Operation(summary = "Assign a role to a user", description = "Assigns a specified role to the user identified by userId in Keycloak.", responses = {
                        @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "User or role not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public void addUserRole(
                        @PathVariable String userId,
                        @RequestParam String roleName) {
                keycloakService.assignRoleToUser(userId, roleName);
        }

        @PostMapping("/{userId}/roles/remove")
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
