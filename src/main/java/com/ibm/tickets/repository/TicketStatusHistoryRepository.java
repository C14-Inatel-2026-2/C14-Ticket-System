package com.ibm.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.tickets.model.TicketStatusHistory;

/**
 * Provides database operations for ticket status history.
 *
 * @author Gabriel Guimaraes
 */
public interface TicketStatusHistoryRepository
        extends JpaRepository<TicketStatusHistory, Long> {

    List<TicketStatusHistory>
            findAllByTicketIdOrderByChangedAtDesc(Long ticketId);
}