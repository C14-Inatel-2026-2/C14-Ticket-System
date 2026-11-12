
package com.ibm.tickets.dto;
/**
 * @author Rodrigo Costa
 * UserOptionResponse
 * @param id
 * @param name
 * @param email
 * @param role
 */
public record UserOptionResponse(
        Long id,
        String name,
        String email,
        String role
) {
}
