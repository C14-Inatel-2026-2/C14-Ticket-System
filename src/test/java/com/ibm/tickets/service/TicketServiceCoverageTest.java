package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.ibm.tickets.dto.*;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.*;
import com.ibm.tickets.repository.*;

/** Tests project filters, reference validation and deadline auditing. @author Gabriel Guimaraes */
@ExtendWith(MockitoExtension.class)
class TicketServiceCoverageTest {
    @Mock TicketRepository tickets;
    @Mock ProjectRepository projects;
    @Mock UserRepository users;
    @Mock TicketActivityService activities;
    @InjectMocks TicketService service;

    @Test void projectOnlyFilterMapsTicketsInRepositoryOrder() {
        Ticket first = ticket("First"), second = ticket("Second");
        when(tickets.findAllByProjectReference_IdOrderByCreatedAtDesc(7L)).thenReturn(List.of(first, second));
        assertEquals(List.of(TicketResponse.from(first), TicketResponse.from(second)), service.findAll(7L, null, null));
        verify(tickets).findAllByProjectReference_IdOrderByCreatedAtDesc(7L);
        verifyNoMoreInteractions(tickets);
    }
    @Test void projectAndStatusFilterSelectsMatchingRepositoryQuery() {
        Ticket ticket = ticket("Status");
        when(tickets.findAllByProjectReference_IdAndStatusOrderByCreatedAtDesc(7L, TicketStatus.OPEN)).thenReturn(List.of(ticket));
        assertEquals(List.of(TicketResponse.from(ticket)), service.findAll(7L, TicketStatus.OPEN, null));
        verify(tickets).findAllByProjectReference_IdAndStatusOrderByCreatedAtDesc(7L, TicketStatus.OPEN);
        verifyNoMoreInteractions(tickets);
    }
    @Test void projectAndPriorityFilterSelectsMatchingRepositoryQuery() {
        Ticket ticket = ticket("Priority");
        when(tickets.findAllByProjectReference_IdAndPriorityOrderByCreatedAtDesc(7L, TicketPriority.HIGH)).thenReturn(List.of(ticket));
        assertEquals(List.of(TicketResponse.from(ticket)), service.findAll(7L, null, TicketPriority.HIGH));
        verify(tickets).findAllByProjectReference_IdAndPriorityOrderByCreatedAtDesc(7L, TicketPriority.HIGH);
        verifyNoMoreInteractions(tickets);
    }
    @Test void combinesAllProjectFilters() {
        Ticket ticket = ticket("Combined");
        when(tickets.findAllByProjectReference_IdAndStatusAndPriorityOrderByCreatedAtDesc(7L, TicketStatus.OPEN, TicketPriority.HIGH)).thenReturn(List.of(ticket));
        assertEquals(List.of(TicketResponse.from(ticket)), service.findAll(7L, TicketStatus.OPEN, TicketPriority.HIGH));
        verify(tickets).findAllByProjectReference_IdAndStatusAndPriorityOrderByCreatedAtDesc(7L, TicketStatus.OPEN, TicketPriority.HIGH);
        verifyNoMoreInteractions(tickets);
    }
    @Test void rejectsMissingProjectBeforeLookingUpAssigneeOrSaving() {
        when(projects.findById(7L)).thenReturn(Optional.empty());
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.create(request(null)));
        assertEquals("Project not found: 7", error.getMessage());
        verifyNoInteractions(users, tickets, activities);
    }
    @Test void rejectsMissingProjectOnUpdateWithoutSavingOrAuditing() {
        when(tickets.findById(1L)).thenReturn(Optional.of(ticket("Before")));
        when(projects.findById(7L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, request(null), "Editor"));
        verify(tickets, never()).saveAndFlush(any());
        verifyNoInteractions(users, activities);
    }
    @Test void rejectsMissingAssigneeOnUpdateWithoutSavingOrAuditing() {
        when(tickets.findById(1L)).thenReturn(Optional.of(ticket("Before")));
        when(projects.findById(7L)).thenReturn(Optional.of(project()));
        when(users.findById(2L)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.update(1L, request(null), "Editor"));
        assertEquals(400, error.getStatusCode().value());
        assertEquals("Usuário responsável não encontrado", error.getReason());
        verify(tickets, never()).saveAndFlush(any());
        verifyNoInteractions(activities);
    }
    @Test void addsDeadlineAndUsesPersistedAssigneeName() {
        Ticket ticket = ticket("Before");
        User user = new User("Canonical name", "user@example.com", UserRole.DEVELOPER);
        ReflectionTestUtils.setField(user, "id", 2L);
        when(tickets.findById(1L)).thenReturn(Optional.of(ticket));
        when(projects.findById(7L)).thenReturn(Optional.of(project()));
        when(users.findById(2L)).thenReturn(Optional.of(user));
        when(tickets.saveAndFlush(ticket)).thenReturn(ticket);
        LocalDate date = LocalDate.of(2099, 3, 4);
        TicketResponse response = service.update(1L, request(date), "Editor");
        assertEquals(date, response.dueDate());
        assertEquals("Canonical name", response.assignee());
        assertEquals(2L, response.assigneeUserId());
        assertEquals(7L, response.projectId());
        assertEquals("New title", ticket.getTitle());
        assertEquals("New description", ticket.getDescription());
        assertSame(user, ticket.getUser());
        verify(activities).record(ticket, "Editor", "DUE_DATE_CHANGED", null, "2099-03-04");
        verify(activities).record(ticket, "Editor", "ASSIGNEE_CHANGED", "Old assignee", "Canonical name");
        verify(activities).record(ticket, "Editor", "PRIORITY_CHANGED", "HIGH", "MEDIUM");
    }
    @Test void missingTicketCannotBeUpdatedOrDeleted() {
        when(tickets.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TicketNotFoundException.class, () -> service.update(99L, request(null), "Editor"));
        assertThrows(TicketNotFoundException.class, () -> service.updateStatus(99L, TicketStatus.CLOSED, "Editor"));
        assertThrows(TicketNotFoundException.class, () -> service.delete(99L));
        verify(tickets, never()).saveAndFlush(any());
        verify(tickets, never()).delete(any());
        verifyNoInteractions(projects, users, activities);
    }
    private TicketRequest request(LocalDate due) {
        return new TicketRequest("New title", "New description", 7L, "Untrusted display name", 2L, TicketPriority.MEDIUM, due);
    }
    private Ticket ticket(String title) {
        Ticket ticket = new Ticket(title, "Description", TicketPriority.HIGH);
        ticket.setAssignee("Old assignee");
        return ticket;
    }
    private Project project() {
        Project project = new Project("Team", "Description");
        ReflectionTestUtils.setField(project, "id", 7L);
        return project;
    }
}
