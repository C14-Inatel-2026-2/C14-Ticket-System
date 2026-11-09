
package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.ibm.tickets.dto.TicketStatusHistoryResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.TicketStatusHistory;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.TicketStatusHistoryRepository;

class TicketHistoryServiceTest {

    private TicketRepository ticketRepository;
    private TicketStatusHistoryRepository historyRepository;
    private TicketService ticketService;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticketRepository = mock(TicketRepository.class);
        historyRepository = mock(TicketStatusHistoryRepository.class);

        ticketService = new TicketService(
                ticketRepository,
                historyRepository
        );

        ticket = new Ticket(
                "Erro no login",
                "Problema de autenticação",
                TicketPriority.MEDIUM
        );
    }

    @Test
    void shouldCreateHistoryWhenStatusChanges() {
        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        ticketService.updateStatus(1L, TicketStatus.IN_PROGRESS);

        ArgumentCaptor<TicketStatusHistory> captor =
                ArgumentCaptor.forClass(TicketStatusHistory.class);

        verify(historyRepository).saveAndFlush(captor.capture());

        TicketStatusHistory history = captor.getValue();

        assertEquals(TicketStatus.OPEN, history.getPreviousStatus());
        assertEquals(TicketStatus.IN_PROGRESS, history.getNewStatus());
        assertSame(ticket, history.getTicket());
    }

    @Test
    void shouldNotCreateHistoryForSameStatus() {
        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        ticketService.updateStatus(1L, TicketStatus.OPEN);

        verifyNoInteractions(historyRepository);
        verify(ticketRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldCreateHistoryForEachStatusChange() {
        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        ticketService.updateStatus(1L, TicketStatus.IN_PROGRESS);
        ticketService.updateStatus(1L, TicketStatus.RESOLVED);

        ArgumentCaptor<TicketStatusHistory> captor =
                ArgumentCaptor.forClass(TicketStatusHistory.class);

        verify(historyRepository, times(2))
                .saveAndFlush(captor.capture());

        List<TicketStatusHistory> histories =
                captor.getAllValues();

        assertEquals(
                TicketStatus.OPEN,
                histories.get(0).getPreviousStatus()
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                histories.get(0).getNewStatus()
        );

        assertEquals(
                TicketStatus.IN_PROGRESS,
                histories.get(1).getPreviousStatus()
        );

        assertEquals(
                TicketStatus.RESOLVED,
                histories.get(1).getNewStatus()
        );
    }

    @Test
    void shouldReturnEmptyHistory() {
        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(historyRepository.findAllByTicketIdOrderByChangedAtDesc(1L))
                .thenReturn(List.of());

        List<TicketStatusHistoryResponse> history =
                ticketService.findStatusHistory(1L);

        assertTrue(history.isEmpty());
    }

    @Test
    void shouldRejectHistoryForNonexistentTicket() {
        when(ticketRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.findStatusHistory(999L)
        );

        verifyNoInteractions(historyRepository);
    }
}
