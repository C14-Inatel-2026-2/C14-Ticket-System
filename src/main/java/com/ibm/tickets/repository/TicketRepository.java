package com.ibm.tickets.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;

/**
 * Provides database operations for tickets.
 *
 * @author Gabriel Guimaraes
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findAllByPriorityOrderByCreatedAtDesc(
            TicketPriority priority);

    List<Ticket> findAllByStatusOrderByCreatedAtDesc(
            TicketStatus status);

    List<Ticket> findAllByStatusAndPriorityOrderByCreatedAtDesc(
            TicketStatus status,
            TicketPriority priority);

    long countByAssigneeUserId(Long userId);
    long countByAssigneeUserIdAndStatusIn(Long userId, Collection<TicketStatus> statuses);

    List<Ticket> findAllByAssigneeUserIdOrderByCreatedAtDesc(Long userId);
}
