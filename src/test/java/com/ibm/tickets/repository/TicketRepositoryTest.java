package com.ibm.tickets.repository;

import java.util.List;
import java.time.OffsetDateTime;
import jakarta.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;

/**
 * Integration tests for TicketRepository.
 *
 * @author Gabriel Guimaraes
 */
@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldListTicketsByCreationDateDescending() {
        Ticket firstTicket = createTicket(
                "First ticket",
                TicketStatus.OPEN);

        ticketRepository.saveAndFlush(firstTicket);

        Ticket secondTicket = createTicket(
                "Second ticket",
                TicketStatus.IN_PROGRESS);

        ticketRepository.saveAndFlush(secondTicket);

        // Explicit timestamps make this ordering test independent of clock resolution.
        entityManager.createNativeQuery("update tickets set created_at = :time where id = :id")
                .setParameter("time", OffsetDateTime.parse("2026-01-01T10:00:00Z"))
                .setParameter("id", firstTicket.getId()).executeUpdate();
        entityManager.createNativeQuery("update tickets set created_at = :time where id = :id")
                .setParameter("time", OffsetDateTime.parse("2026-01-01T11:00:00Z"))
                .setParameter("id", secondTicket.getId()).executeUpdate();
        entityManager.clear();

        List<Ticket> tickets = ticketRepository
                .findAllByOrderByCreatedAtDesc();

        assertAll(
                () -> assertEquals(2, tickets.size()),
                () -> assertEquals(
                        "Second ticket",
                        tickets.get(0).getTitle()),
                () -> assertEquals(
                        "First ticket",
                        tickets.get(1).getTitle()));
    }

    @Test
    void shouldFilterTicketsByStatus() {
        Ticket openTicket = createTicket(
                "Open ticket",
                TicketStatus.OPEN);

        Ticket closedTicket = createTicket(
                "Closed ticket",
                TicketStatus.CLOSED);

        ticketRepository.saveAndFlush(openTicket);
        ticketRepository.saveAndFlush(closedTicket);

        List<Ticket> tickets = ticketRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        TicketStatus.CLOSED);

        assertAll(
                () -> assertEquals(1, tickets.size()),
                () -> assertEquals(
                        "Closed ticket",
                        tickets.get(0).getTitle()),
                () -> assertEquals(
                        TicketStatus.CLOSED,
                        tickets.get(0).getStatus()));
    }

    private Ticket createTicket(
            String title,
            TicketStatus status) {
        Ticket ticket = new Ticket(
                title,
                "Repository integration test",
                TicketPriority.HIGH);

        ticket.setProject("Ticket System");
        ticket.setAssignee("Gabriel");
        ticket.setStatus(status);

        return ticket;
    }
}