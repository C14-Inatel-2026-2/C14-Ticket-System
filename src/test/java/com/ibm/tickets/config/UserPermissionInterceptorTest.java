package com.ibm.tickets.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.ibm.tickets.model.UserRole;

/** @author Gabriel Guimaraes */
class UserPermissionInterceptorTest {
    private final UserPermissionInterceptor interceptor = new UserPermissionInterceptor();

    @Test
    void unrelatedRoutesDoNotRequireSession() throws Exception {
        for (String[] route : new String[][] {{"GET", "/api/users"}, {"DELETE", "/api/users/name"},
                {"POST", "/api/users/1"}, {"POST", "/users/manage"}, {"GET", "/assets/app.css"}}) {
            MockHttpServletRequest request = new MockHttpServletRequest(route[0], route[1]);
            assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
            assertNull(request.getSession(false));
        }
    }

    @Test
    void protectedApiRequiresAuthenticatedSession() throws Exception {
        for (String[] route : new String[][] {{"POST", "/api/users"}, {"DELETE", "/api/users/42"}}) {
            MockHttpServletRequest request = new MockHttpServletRequest(route[0], route[1]);
            MockHttpServletResponse response = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(request, response, new Object()));
            assertEquals(401, response.getStatus());
            request.getSession();
            response = new MockHttpServletResponse();
            assertFalse(interceptor.preHandle(request, response, new Object()));
            assertEquals(401, response.getStatus());
        }
    }

    @Test
    void managementPageRedirectsToContextRootWhenAnonymous() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/tickets/users/manage");
        request.setContextPath("/tickets");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals("/tickets/", response.getRedirectedUrl());
    }

    @Test
    void onlyAdministratorAndProjectManagerCanManageUsers() throws Exception {
        for (UserRole role : UserRole.values()) {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
            request.getSession().setAttribute("loggedUser", "Alice");
            request.getSession().setAttribute("userRole", role.name());
            MockHttpServletResponse response = new MockHttpServletResponse();
            boolean allowed = role == UserRole.ADMIN || role == UserRole.PROJECT_MANAGER;
            assertEquals(allowed, interceptor.preHandle(request, response, new Object()), role.name());
            assertEquals(allowed ? 200 : 403, response.getStatus());
        }
        MockHttpServletRequest missingRole = new MockHttpServletRequest("GET", "/users/manage");
        missingRole.getSession().setAttribute("loggedUser", "Alice");
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(missingRole, response, new Object()));
        assertEquals(403, response.getStatus());
    }
}
