package com.ibm.tickets.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.repository.TicketRepository;

/**
 * Unit tests for TicketService.
 *
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void shouldCreateTicket() {
        TicketRequest request = new TicketRequest(
                "Printer problem",
                "The printer is not working",
                "Internal Systems",
                "Gabriel",
                TicketPriority.HIGH);

        when(ticketRepository.saveAndFlush(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.create(request);

        assertAll(
                () -> assertEquals(
                        "Printer problem",
                        response.title()),
                () -> assertEquals(
                        "The printer is not working",
                        response.description()),
                () -> assertEquals(
                        "Internal Systems",
                        response.project()),
                () -> assertEquals(
                        "Gabriel",
                        response.assignee()),
                () -> assertEquals(
                        TicketPriority.HIGH,
                        response.priority()));

        verify(ticketRepository).saveAndFlush(any(Ticket.class));
    }

    @Test
    void shouldListAllTickets() {
        Ticket ticket = createTicket(
                "Printer problem",
                TicketStatus.OPEN);

        when(ticketRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(ticket));

        List<TicketResponse> responses = ticketService.findAll(null);

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(
                        "Printer problem",
                        responses.get(0).title()),
                () -> assertEquals(
                        "Internal Systems",
                        responses.get(0).project()),
                () -> assertEquals(
                        TicketStatus.OPEN,
                        responses.get(0).status()));

        verify(ticketRepository)
                .findAllByOrderByCreatedAtDesc();
    }

    @Test
    void shouldFilterTicketsByStatus() {
        Ticket ticket = createTicket(
                "Database problem",
                TicketStatus.IN_PROGRESS);

        when(ticketRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        TicketStatus.IN_PROGRESS))
                .thenReturn(List.of(ticket));

        List<TicketResponse> responses = ticketService.findAll(
                TicketStatus.IN_PROGRESS);

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(
                        "Database problem",
                        responses.get(0).title()),
                () -> assertEquals(
                        TicketStatus.IN_PROGRESS,
                        responses.get(0).status()));

        verify(ticketRepository)
                .findAllByStatusOrderByCreatedAtDesc(
                        TicketStatus.IN_PROGRESS);
    }

    @Test
    void shouldThrowExceptionWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.findById(99L));

        verify(ticketRepository).findById(99L);
    }
    
    @Test
    void shouldUpdateTicket() {
        Ticket ticket = createTicket(
                "Old title",
                TicketStatus.OPEN);

        TicketRequest request = new TicketRequest(
                "Updated title",
                "Updated description",
                "Updated project",
                "Mariana",
                TicketPriority.MEDIUM);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        TicketResponse response = ticketService.update(
                1L,
                request);

        assertAll(
                () -> assertEquals(
                        "Updated title",
                        response.title()),
                () -> assertEquals(
                        "Updated description",
                        response.description()),
                () -> assertEquals(
                        "Updated project",
                        response.project()),
                () -> assertEquals(
                        "Mariana",
                        response.assignee()),
                () -> assertEquals(
                        TicketPriority.MEDIUM,
                        response.priority()));

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).saveAndFlush(ticket);
    }

    @Test
    void shouldUpdateTicketStatus() {
        Ticket ticket = createTicket(
                "Printer problem",
                TicketStatus.OPEN);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        TicketResponse response = ticketService.updateStatus(
                1L,
                TicketStatus.CLOSED);

        assertEquals(
                TicketStatus.CLOSED,
                response.status());

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).saveAndFlush(ticket);
    }

    @Test
    void shouldDeleteTicket() {
        Ticket ticket = createTicket(
                "Printer problem",
                TicketStatus.OPEN);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        ticketService.delete(1L);

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).delete(ticket);
    }

    private Ticket createTicket(
            String title,
            TicketStatus status) {
        Ticket ticket = new Ticket(
                title,
                "Ticket description",
                TicketPriority.HIGH);

        ticket.setProject("Internal Systems");
        ticket.setAssignee("Gabriel");
        ticket.setStatus(status);

        return ticket;
    }
}