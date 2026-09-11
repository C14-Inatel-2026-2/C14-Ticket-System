package com.ibm.tickets.dto;

import java.time.OffsetDateTime;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;

/**
 * Represents ticket data returned by the API.
 *
 * @param id ticket identifier
 * @param title ticket title
 * @param description ticket description
 * @param status current ticket status
 * @param priority ticket priority
 * @param createdAt creation date
 * @param updatedAt last update date
 * @author Gabriel Guimaraes
 */
public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}