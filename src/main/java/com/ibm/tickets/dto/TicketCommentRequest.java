package com.ibm.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * TicketCommentRequest
 * @author Rodrigo Costa
 * @param content
 */
public record TicketCommentRequest(
    @NotBlank 
    @Size(max = 5000)
    String content
){}