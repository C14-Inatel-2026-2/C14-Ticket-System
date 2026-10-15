package com.ibm.tickets.dto;

import java.util.List;

/**
 * @author Rodrigo Fraga da Costa
 * UserProfileResponse
 * @param name
 * @param email
 * @param role
 * @param assignedTickets
 * @param completedTickets
 * @param projects
 */
public record UserProfileResponse(String name,
    String email,
    String role,
    int assignedTickets,
    int completedTickets,
    List<String> projects
) {
    
}
