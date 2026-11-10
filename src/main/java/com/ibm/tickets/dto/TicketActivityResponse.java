package com.ibm.tickets.dto;

import java.time.OffsetDateTime;

/**
 * @author Rodrigo Fraga da Costa
 * TicketActivityResponse
 * @param type
 * @param author
 * @param activityType
 * @param oldValue
 * @param newValue
 * @param content
 * @param createdAt
 */
public record TicketActivityResponse(
    String type,
    String author,
    String activityType,
    String oldValue,
    String newValue,
    String content,
    OffsetDateTime createdAt
) {
    
}
