package com.eridanimelo.user_api.dto;

import org.keycloak.representations.idm.UserRepresentation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {
    private UserRepresentation user;
    private String password;
}
