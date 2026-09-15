package com.ibm.tickets.service;

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
    void shouldThrowExceptionWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.findById(99L));

        verify(ticketRepository).findById(99L);
    }
}