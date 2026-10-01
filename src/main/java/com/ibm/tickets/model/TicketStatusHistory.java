package com.ibm.tickets.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Represents a ticket status change stored in the database.
 *
 * @author Gabriel Guimaraes
 */
@Entity
@Table(name = "ticket_status_history")
public class TicketStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", nullable = false, length = 50)
    private TicketStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private TicketStatus newStatus;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    protected TicketStatusHistory() {
    }

    public TicketStatusHistory(
            Ticket ticket,
            TicketStatus previousStatus,
            TicketStatus newStatus) {
        this.ticket = ticket;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    @PrePersist
    private void beforeInsert() {
        changedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public TicketStatus getPreviousStatus() {
        return previousStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }
}