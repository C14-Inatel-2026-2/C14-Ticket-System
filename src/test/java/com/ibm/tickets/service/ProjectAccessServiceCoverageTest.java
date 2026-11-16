package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.ProjectRepository;
import com.ibm.tickets.repository.TicketRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** @author Gabriel Guimaraes */
class ProjectAccessServiceCoverageTest {
    private final ProjectRepository projects = mock(ProjectRepository.class);
    private final TicketRepository tickets = mock(TicketRepository.class);
    private final ProjectAccessService service = new ProjectAccessService(projects, tickets);

    @Test
    void globalAccessIsLimitedToAdministratorsAndProjectManagers() {
        for (UserRole role : UserRole.values()) {
            assertEquals(
                role == UserRole.ADMIN || role == UserRole.PROJECT_MANAGER,
                service.hasGlobalAccess(session(role.name()))
            );
        }
    }

    @Test
    void rolesRequireAuthenticatedSessionAndValidStringEnum() {
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.hasGlobalAccess(null));
        HttpSession missingUser = mock(HttpSession.class);
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.hasGlobalAccess(missingUser));
        HttpSession invalid = session(null);
        assertStatus(HttpStatus.FORBIDDEN, () -> service.hasGlobalAccess(invalid));
        when(invalid.getAttribute("userRole")).thenReturn(UserRole.ADMIN);
        assertStatus(HttpStatus.FORBIDDEN, () -> service.hasGlobalAccess(invalid));
        when(invalid.getAttribute("userRole")).thenReturn("unknown");
        assertStatus(HttpStatus.FORBIDDEN, () -> service.hasGlobalAccess(invalid));
    }

    @Test
    void identityMustBeLongAndPresent() {
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.getUserId(null));
        HttpSession session = mock(HttpSession.class);
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.getUserId(session));
        when(session.getAttribute("userId")).thenReturn("4");
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.getUserId(session));
        when(session.getAttribute("userId")).thenReturn(4L);
        assertEquals(4L, service.getUserId(session));
    }

    @Test
    void validatesProjectBeforeAllowingGlobalAccess() {
        assertStatus(HttpStatus.BAD_REQUEST, () -> service.checkAccess(null, null));
        verifyNoInteractions(projects);
        assertStatus(HttpStatus.NOT_FOUND, () -> service.checkAccess(7L, session("ADMIN")));
        when(projects.existsById(7L)).thenReturn(true);
        service.checkAccess(7L, session("ADMIN"));
        service.checkAccess(7L, session("PROJECT_MANAGER"));
        verify(projects, never()).existsByIdAndMembers_Id(any(), any());
    }

    @Test
    void ordinaryUsersMustBelongToProject() {
        when(projects.existsById(7L)).thenReturn(true);
        HttpSession session = session("DEVELOPER");
        when(session.getAttribute("userId")).thenReturn(4L);
        assertStatus(HttpStatus.FORBIDDEN, () -> service.checkAccess(7L, session));
        when(projects.existsByIdAndMembers_Id(7L, 4L)).thenReturn(true);
        assertDoesNotThrow(() -> service.checkAccess(7L, session));
        when(session.getAttribute("userId")).thenReturn(null);
        assertStatus(HttpStatus.UNAUTHORIZED, () -> service.checkAccess(7L, session));
    }

    @Test
    void ticketAccessRequiresTicketProjectAndMembership() {
        assertThrows(TicketNotFoundException.class, () -> service.checkTicketAccess(3L, null));
        Ticket ticket = new Ticket("Fix", "Details", TicketPriority.HIGH);
        when(tickets.findById(3L)).thenReturn(Optional.of(ticket));
        assertStatus(HttpStatus.NOT_FOUND, () -> service.checkTicketAccess(3L, null));
        Project project = mock(Project.class);
        when(project.getId()).thenReturn(7L);
        ticket.setProjectReference(project);
        when(projects.existsById(7L)).thenReturn(true);
        HttpSession session = session("TESTER");
        when(session.getAttribute("userId")).thenReturn(4L);
        assertStatus(HttpStatus.FORBIDDEN, () -> service.checkTicketAccess(3L, session));
        when(projects.existsByIdAndMembers_Id(7L, 4L)).thenReturn(true);
        assertDoesNotThrow(() -> service.checkTicketAccess(3L, session));
    }

    private HttpSession session(String role) {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("loggedUser")).thenReturn("Ana");
        when(session.getAttribute("userRole")).thenReturn(role);
        return session;
    }
    private void assertStatus(
            HttpStatus status,
            org.junit.jupiter.api.function.Executable action) {
        var exception = assertThrows(ResponseStatusException.class, action);
        assertEquals(status, exception.getStatusCode());
    }
}
