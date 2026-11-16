package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.ibm.tickets.dto.CreateUserRequest;
import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.User;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

/** @author Gabriel Guimaraes */
class UserServiceCoverageTest {
    private final UserRepository users = mock(UserRepository.class);
    private final TicketRepository tickets = mock(TicketRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final UserService service = new UserService(users, tickets, encoder);

    @Test
    void profileIncludesSortedProjectsAndCompletedCounts() {
        User user = new User("Ana", "ana@example.com", UserRole.DEVELOPER);
        user.getProjects().add(new Project("Zulu", "last"));
        user.getProjects().add(new Project("Alpha", "first"));
        when(users.findById(4L)).thenReturn(Optional.of(user));
        when(tickets.countByAssigneeUserId(4L)).thenReturn(8L);
        when(tickets.countByAssigneeUserIdAndStatusIn(
            4L, List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED)
        )).thenReturn(3L);
        var profile = service.getProfile(4L);
        assertAll(
            () -> assertEquals("Ana", profile.name()),
            () -> assertEquals("ana@example.com", profile.email()),
            () -> assertEquals("DEVELOPER", profile.role()),
            () -> assertEquals(8, profile.assignedTickets()),
            () -> assertEquals(3, profile.completedTickets()),
            () -> assertEquals(List.of("Alpha", "Zulu"), profile.projects())
        );
    }

    @Test
    void missingProfileDoesNotQueryTicketCounts() {
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getProfile(99L));
        verifyNoInteractions(tickets);
    }

    @Test
    void userListsMapFieldsAndPreserveRepositoryAndRequestedOrder() {
        User ana = user(4L, "Ana");
        User bob = user(7L, "Bob");
        when(users.findAllByOrderByNameAsc()).thenReturn(List.of(ana, bob));
        when(users.findById(4L)).thenReturn(Optional.of(ana));
        when(users.findById(7L)).thenReturn(Optional.of(bob));
        var all = service.getAllUsers();
        assertEquals(List.of("Ana", "Bob"), all.stream().map(v -> v.name()).toList());
        assertEquals("Ana@example.com", all.get(0).email());
        assertEquals("REPORTER", all.get(0).role());
        var requestedUsers = service.getAllUsersByIds(List.of(7L, 4L, 7L));
        assertEquals(
            List.of(7L, 4L, 7L),
            requestedUsers.stream().map(v -> v.id()).toList()
        );
        assertTrue(service.getAllUsersByIds(List.of()).isEmpty());
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getAllUsersByIds(List.of(99L)));
    }

    @Test
    void creationNormalizesEmailEncodesPasswordAndReturnsSavedIdentity() {
        when(encoder.encode("secret123")).thenReturn("hashed");
        when(users.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User candidate = invocation.getArgument(0);
            assertEquals("Alice", candidate.getName());
            assertEquals("alice@example.com", candidate.getEmail());
            assertEquals(UserRole.TESTER, candidate.getRole());
            assertEquals("hashed", candidate.getPasswordHash());
            return user(12L, "Alice");
        });
        var result = service.createUser(request());
        assertEquals(12L, result.id());
        verify(users).existsByEmailIgnoreCase("alice@example.com");
        verify(encoder).encode("secret123");
    }

    @Test
    void duplicateEmailStopsBeforeEncodingOrSaving() {
        when(users.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);
        assertStatus(HttpStatus.CONFLICT, () -> service.createUser(request()));
        verifyNoInteractions(encoder);
        verify(users, never()).saveAndFlush(any());
    }

    @Test
    void concurrentDuplicateIsTranslatedToConflictWithOriginalCause() {
        var cause = new DataIntegrityViolationException("duplicate email");
        when(users.saveAndFlush(any())).thenThrow(cause);
        var exception = assertStatus(HttpStatus.CONFLICT, () -> service.createUser(request()));
        assertSame(cause, exception.getCause());
    }

    @Test
    void deletionGuardsSelfMissingAndAssignedUsers() {
        assertStatus(HttpStatus.FORBIDDEN, () -> service.deleteUser(4L, 4L));
        verifyNoInteractions(users, tickets);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.deleteUser(5L, 4L));
        User busyUser = user(6L, "Busy");
        when(users.findById(6L)).thenReturn(Optional.of(busyUser));
        when(tickets.countByAssigneeUserId(6L)).thenReturn(1L);
        assertStatus(HttpStatus.CONFLICT, () -> service.deleteUser(6L, 4L));
        verify(users, never()).delete(any());
        verify(users, never()).flush();
    }

    @Test
    void unassignedUserCanBeDeletedAndFlushed() {
        User user = user(5L, "Free");
        when(users.findById(5L)).thenReturn(Optional.of(user));
        service.deleteUser(5L, 4L);
        var order = inOrder(users);
        order.verify(users).delete(user);
        order.verify(users).flush();
    }

    @Test
    void assignedTicketsRequireExistingUserAndMapTicketDetails() {
        assertStatus(HttpStatus.NOT_FOUND, () -> service.getAssignedTickets(99L));
        verifyNoInteractions(tickets);
        when(users.existsById(4L)).thenReturn(true);
        Ticket ticket = new Ticket("Fix", "Broken login", TicketPriority.HIGH);
        when(tickets.findAllByAssigneeUserIdOrderByCreatedAtDesc(4L)).thenReturn(List.of(ticket));
        var result = service.getAssignedTickets(4L);
        assertEquals(1, result.size());
        assertEquals("Fix", result.get(0).title());
        assertEquals("Broken login", result.get(0).description());
        assertEquals(TicketPriority.HIGH, result.get(0).priority());
        assertEquals(TicketStatus.OPEN, result.get(0).status());
    }

    private CreateUserRequest request() {
        return new CreateUserRequest(
            "Alice", " ALICE@EXAMPLE.COM ", "secret123", UserRole.TESTER
        );
    }
    private User user(Long id, String name) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(name + "@example.com");
        when(user.getRole()).thenReturn(UserRole.REPORTER);
        return user;
    }
    private ResponseStatusException assertStatus(
            HttpStatus status,
            org.junit.jupiter.api.function.Executable action) {
        var exception = assertThrows(ResponseStatusException.class, action);
        assertEquals(status, exception.getStatusCode());
        return exception;
    }
}
