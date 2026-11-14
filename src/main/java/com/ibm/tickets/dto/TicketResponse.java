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
 * @param projectId related project identifier
 * @param project related project
 * @param assignee person responsible for the ticket
 * @param assigneeUserId identifier of the assigned user
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
        Long projectId,
        String project,
        String assignee,
        Long assigneeUserId,
        TicketStatus status,
        TicketPriority priority,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getProjectReference() != null ? ticket.getProjectReference().getId() : null,
                ticket.getProject(),
                ticket.getAssignee(),
                ticket.getUser() != null ? ticket.getUser().getId() : null,
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }
}