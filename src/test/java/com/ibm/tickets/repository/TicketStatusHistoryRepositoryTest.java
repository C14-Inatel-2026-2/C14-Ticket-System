package com.ibm.tickets.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.TicketStatusHistory;

/**
 * Integration tests for TicketStatusHistoryRepository.
 *
 * @author Gabriel Guimaraes
 */
@DataJpaTest
class TicketStatusHistoryRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketStatusHistoryRepository historyRepository;

    @Test
    void shouldFindHistoryOrderedByChangedAtDescending() {
        Ticket ticket = createAndSaveTicket();

        TicketStatusHistory firstChange =
                new TicketStatusHistory(
                        ticket,
                        TicketStatus.OPEN,
                        TicketStatus.IN_PROGRESS);

        historyRepository.saveAndFlush(firstChange);

        TicketStatusHistory secondChange =
                new TicketStatusHistory(
                        ticket,
                        TicketStatus.IN_PROGRESS,
                        TicketStatus.CLOSED);

        historyRepository.saveAndFlush(secondChange);

        List<TicketStatusHistory> history =
                historyRepository
                        .findAllByTicketIdOrderByChangedAtDesc(
                                ticket.getId());

        assertAll(
                () -> assertEquals(2, history.size()),
                () -> assertEquals(
                        TicketStatus.CLOSED,
                        history.get(0).getNewStatus()),
                () -> assertEquals(
                        TicketStatus.IN_PROGRESS,
                        history.get(1).getNewStatus()),
                () -> assertEquals(
                        ticket.getId(),
                        history.get(0).getTicket().getId()));
    }

    @Test
    void shouldReturnEmptyHistoryWhenTicketHasNoChanges() {
        Ticket ticket = createAndSaveTicket();

        List<TicketStatusHistory> history =
                historyRepository
                        .findAllByTicketIdOrderByChangedAtDesc(
                                ticket.getId());

        assertTrue(history.isEmpty());
    }

    private Ticket createAndSaveTicket() {
        Ticket ticket = new Ticket(
                "Printer problem",
                "The printer is not working",
                TicketPriority.HIGH);

        ticket.setProject("Internal Systems");
        ticket.setAssignee("Gabriel");

        return ticketRepository.saveAndFlush(ticket);
    }
}