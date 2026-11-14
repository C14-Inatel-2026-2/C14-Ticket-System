package com.ibm.tickets.dto;

import com.ibm.tickets.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Rodrigo Fraga da Costa
 * CreateUserRequest
 * @param name
 * @param email
 * @param password
 * @param role
 */
public record CreateUserRequest(

    @NotBlank 
    @Size(max = 100)
    String name,

    @NotBlank 
    @Email 
    @Size(max = 150)
    String email,

    @NotBlank 
    @Size(min = 8, max = 72)
    String password,

    @NotNull 
    UserRole role
) {
    
}
