package com.ibm.tickets.controller;

import com.ibm.tickets.controller.Web.LoginController;
import com.ibm.tickets.model.User;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.service.AuthenticationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Verifies login session rotation and logout cleanup.
 * @author Gabriel Guimaraes
 */
class LoginControllerSessionTest {
    private final AuthenticationService authentication = mock(AuthenticationService.class);
    private final LoginController controller = new LoginController(authentication);

    @Test
    void rejectsBothMissingFieldsWithoutAuthenticatingOrOpeningSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        assertEquals("redirect:/?error=email&error=password", controller.login(null, null, request));
        assertEquals("redirect:/?error=email&error=password", controller.login(" ", " ", request));
        assertNull(request.getSession(false));
        verifyNoInteractions(authentication);
    }

    @Test
    void rotatesExistingSessionAndClearsGlobalAdminForPersistedLogin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("globalAdmin", true);
        request.setSession(session);
        String previousId = session.getId();
        User user = new User("Gabriel", "user@example.com", UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", 7L);
        when(authentication.authenticate("user@example.com", "password")).thenReturn(Optional.of(user));

        assertEquals("redirect:/project", controller.login("user@example.com", "password", request));
        assertNotEquals(previousId, session.getId());
        assertEquals(7L, session.getAttribute("userId"));
        assertEquals("Gabriel", session.getAttribute("loggedUser"));
        assertEquals("ADMIN", session.getAttribute("userRole"));
        assertNull(session.getAttribute("globalAdmin"));
    }

    @Test
    void developmentAdministratorClearsPersistedUserIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 7L);
        request.setSession(session);
        assertEquals("redirect:/project", controller.login("admin", "admin", request));
        assertNull(session.getAttribute("userId"));
        assertEquals(true, session.getAttribute("globalAdmin"));
        assertEquals("admin", session.getAttribute("loggedUser"));
        assertEquals("ADMIN", session.getAttribute("userRole"));
        verifyNoInteractions(authentication);
    }

    @Test
    void invalidatesExistingSessionAndHandlesLogoutWithoutSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        assertEquals("redirect:/", controller.logout(request));
        assertTrue(session.isInvalid());
        MockHttpServletRequest anonymous = new MockHttpServletRequest();
        assertEquals("redirect:/", controller.logout(anonymous));
        assertNull(anonymous.getSession(false));
    }
}
