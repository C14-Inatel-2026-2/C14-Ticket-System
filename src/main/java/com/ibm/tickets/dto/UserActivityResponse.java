package com.ibm.tickets.dto;

import java.time.LocalDate;

/**
 * @author Rodrigo Fraga da Costa
 * UserActivityResponse
 * @param date
 * @param count
 */
public record UserActivityResponse(LocalDate date,
    int count
) {
    
}
