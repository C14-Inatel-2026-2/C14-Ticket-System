package com.ibm.tickets.repository;

import java.util.List;

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