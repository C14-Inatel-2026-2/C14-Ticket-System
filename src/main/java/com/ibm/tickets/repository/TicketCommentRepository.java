package com.ibm.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.tickets.model.TicketComment;

/**
 * Provides database operations for ticket comments.
 *
 * @author Gabriel Guimaraes
 */
public interface TicketCommentRepository
        extends JpaRepository<TicketComment, Long> {

    List<TicketComment>
            findAllByTicketIdOrderByCreatedAtDesc(Long ticketId);
}