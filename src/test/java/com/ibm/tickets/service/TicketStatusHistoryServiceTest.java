package com.ibm.tickets.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.TicketStatusHistory;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.TicketStatusHistoryRepository;

/**
 * Unit tests for ticket status history operations.
 *
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class TicketStatusHistoryServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketStatusHistoryRepository historyRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void shouldRecordHistoryWhenStatusChanges() {
        Ticket ticket = createTicket();

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        TicketResponse response = ticketService.updateStatus(
                1L,
                TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, response.status());

        verify(ticketRepository).saveAndFlush(ticket);
        verify(historyRepository)
                .saveAndFlush(any(TicketStatusHistory.class));
    }

    @Test
    void shouldNotRecordHistoryWhenStatusDoesNotChange() {
        Ticket ticket = createTicket();

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.updateStatus(
                1L,
                TicketStatus.OPEN);

        assertEquals(TicketStatus.OPEN, response.status());

        verify(ticketRepository, never())
                .saveAndFlush(any(Ticket.class));

        verify(historyRepository, never())
                .saveAndFlush(any(TicketStatusHistory.class));
    }

    private Ticket createTicket() {
        Ticket ticket = new Ticket(
                "Printer problem",
                "The printer is not working",
                TicketPriority.HIGH);

        ticket.setProject("Internal Systems");
        ticket.setAssignee("Gabriel");

        return ticket;
    }
}