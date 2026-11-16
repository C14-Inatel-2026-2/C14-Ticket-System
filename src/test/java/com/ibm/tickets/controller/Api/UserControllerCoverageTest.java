package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.CreateUserRequest;
import com.ibm.tickets.dto.UserOptionResponse;
import com.ibm.tickets.dto.UserProfileResponse;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Verifies session identity and user-operation delegation.
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class UserControllerCoverageTest {
    @Mock
    private UserService service;
    @InjectMocks
    private UserController controller;

    @Test
    void returnsGlobalAdministratorProfileWithoutDatabaseLookup() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("globalAdmin", true);
        var response = controller.getProfile(session);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(new UserProfileResponse("Admin", "admin@localhost", "ADMIN", 0, 0, List.of()),
                response.getBody());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsMissingOrIncorrectSessionIdentity() {
        MockHttpSession session = new MockHttpSession();
        assertEquals(401, controller.getProfile(session).getStatusCode().value());
        assertEquals(401, controller.getMyTickets(session).getStatusCode().value());
        session.setAttribute("userId", "7");
        assertEquals(401, controller.getProfile(session).getStatusCode().value());
        assertEquals(401, controller.getMyTickets(session).getStatusCode().value());
        verifyNoInteractions(service);
    }

    @Test
    void loadsAuthenticatedProfileAndAssignedTickets() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 7L);
        UserProfileResponse profile = new UserProfileResponse(
                "Gabriel", "user@example.com", "DEVELOPER", 0, 0, List.of());
        when(service.getProfile(7L)).thenReturn(profile);
        when(service.getAssignedTickets(7L)).thenReturn(List.of());
        assertSame(profile, controller.getProfile(session).getBody());
        assertEquals(200, controller.getMyTickets(session).getStatusCode().value());
        assertEquals(List.of(), controller.getMyTickets(session).getBody());
        verify(service).getProfile(7L);
        verify(service, times(2)).getAssignedTickets(7L);
    }

    @Test
    void globalAdministratorWithoutUserIdHasNoAssignedTickets() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("globalAdmin", true);
        assertEquals(200, controller.getMyTickets(session).getStatusCode().value());
        assertEquals(List.of(), controller.getMyTickets(session).getBody());
        verifyNoInteractions(service);
    }

    @Test
    void delegatesProfileListingAndCreation() {
        UserProfileResponse profile = new UserProfileResponse(
                "Gabriel", "user@example.com", "ADMIN", 0, 0, List.of());
        UserOptionResponse option = new UserOptionResponse(7L, "Gabriel", "user@example.com", "ADMIN");
        CreateUserRequest request = new CreateUserRequest(
                "Gabriel", "user@example.com", "password123", UserRole.ADMIN);
        when(service.getProfile(7L)).thenReturn(profile);
        when(service.getAllUsers()).thenReturn(List.of(option));
        when(service.createUser(request)).thenReturn(option);
        assertSame(profile, controller.getUserProfile(7L));
        assertEquals(List.of(option), controller.findAllUsers());
        assertSame(option, controller.createUser(request));
    }

    @Test
    void passesCurrentUserIdentityToDeletionGuard() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 7L);
        controller.delete(8L, session);
        verify(service).deleteUser(8L, 7L);
        session.setAttribute("userId", "invalid");
        controller.delete(9L, session);
        verify(service).deleteUser(9L, null);
    }
}
