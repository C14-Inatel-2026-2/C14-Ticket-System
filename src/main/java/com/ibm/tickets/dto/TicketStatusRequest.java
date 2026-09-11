package com.ibm.tickets.dto;

import com.ibm.tickets.model.TicketStatus;

import jakarta.validation.constraints.NotNull;

/**
 * Represents a ticket status update request.
 *
 * @param status new ticket status
 * @author Gabriel Guimaraes
 */
public record TicketStatusRequest(

        @NotNull(message = "Status is required")
        TicketStatus status) {
}