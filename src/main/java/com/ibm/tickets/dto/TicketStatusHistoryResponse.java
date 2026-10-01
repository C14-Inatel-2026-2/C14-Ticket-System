package com.ibm.tickets.dto;

import java.time.OffsetDateTime;

import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.TicketStatusHistory;

/**
 * Represents a ticket status change returned by the API.
 *
 * @param id history entry identifier
 * @param ticketId ticket identifier
 * @param previousStatus status before the change
 * @param newStatus status after the change
 * @param changedAt date and time of the change
 * @author Gabriel Guimaraes
 */
public record TicketStatusHistoryResponse(
        Long id,
        Long ticketId,
        TicketStatus previousStatus,
        TicketStatus newStatus,
        OffsetDateTime changedAt) {

    public static TicketStatusHistoryResponse from(
            TicketStatusHistory history) {
        return new TicketStatusHistoryResponse(
                history.getId(),
                history.getTicket().getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getChangedAt());
    }
}