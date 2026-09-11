package com.ibm.tickets.dto;

import com.ibm.tickets.model.TicketPriority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents the data received to create or update a ticket.
 *
 * @param title ticket title
 * @param description ticket description
 * @param priority ticket priority
 * @author Gabriel Guimaraes
 */
public record TicketRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must have at most 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Priority is required")
        TicketPriority priority) {
}