package com.ibm.tickets.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import com.ibm.tickets.service.ProjectAccessService;

/** @author Gabriel Guimaraes */
class ProjectAccessInterceptorTest {
    private final ProjectAccessService access = mock(ProjectAccessService.class);
    private final ProjectAccessInterceptor interceptor = new ProjectAccessInterceptor(access);

    @Test
    void webTicketPathsUseProjectIdAndExistingSession() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/app/project/73/tickets/9/edit");
        request.setContextPath("/app");
        var session = request.getSession();
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        verify(access).checkAccess(73L, session);
        verifyNoMoreInteractions(access);
    }

    @Test
    void ticketCommentsAndActivitiesUseTicketIdWithoutCreatingSession() {
        for (String suffix : new String[] {"comments", "activities", "comments/12", "activities/12"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets/19/" + suffix);
            assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
            assertNull(request.getSession(false));
        }
        verify(access, times(4)).checkTicketAccess(19L, null);
        verifyNoMoreInteractions(access);
    }

    @Test
    void unrelatedAndMalformedPathsDoNotInvokeAccessService() {
        for (String path : new String[] {"/project/name/tickets", "/project/2", "/api/tickets/3",
                "/api/tickets/3/comments-extra", "/api/tickets/name/comments", "/assets/app.css"}) {
            assertTrue(interceptor.preHandle(new MockHttpServletRequest("GET", path),
                    new MockHttpServletResponse(), new Object()));
        }
        verifyNoInteractions(access);
    }

    @Test
    void accessFailuresPropagateToRequestHandling() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/project/2/tickets");
        IllegalStateException denied = new IllegalStateException("denied");
        doThrow(denied).when(access).checkAccess(2L, null);
        assertSame(denied, assertThrows(IllegalStateException.class,
                () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object())));
    }
}
