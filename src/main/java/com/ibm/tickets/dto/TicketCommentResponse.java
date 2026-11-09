package com.ibm.tickets.dto;

import java.time.OffsetDateTime;

/**
 * TicketCommentResponse
 * @author Rodrigo Costa
 * @param id
 * @param author
 * @param content
 * @param createdAt
 */
public record TicketCommentResponse(
    Long id,
    String author,
    String content,
    OffsetDateTime createdAt
) {
    
}
