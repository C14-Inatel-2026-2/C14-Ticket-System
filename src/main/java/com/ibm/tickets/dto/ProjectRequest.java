package com.ibm.tickets.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Rodrigo Fraga da Costa
 * ProjectRequest
 * @param name
 * @param description
 * @param membersIds
 */
public record ProjectRequest(
    
    @NotBlank(message = "Project name is required")
    @Size (max = 100)
    String name,

    String description,

    @NotNull List< @NotNull Long> membersIds
) {
    
}
