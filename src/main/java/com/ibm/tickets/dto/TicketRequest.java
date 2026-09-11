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
 * @param project related project
 * @param assignee person responsible for the ticket
 * @param priority ticket priority
 * @author Gabriel Guimaraes
 */
public record TicketRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must have at most 150 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Project is required")
        @Size(max = 100, message = "Project must have at most 100 characters")
        String project,

        @NotBlank(message = "Assignee is required")
        @Size(max = 100, message = "Assignee must have at most 100 characters")
        String assignee,

        @NotNull(message = "Priority is required")
        TicketPriority priority) {
}