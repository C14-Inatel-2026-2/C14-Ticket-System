package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.ProjectRequest;
import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.service.ProjectAccessService;
import com.ibm.tickets.service.ProjectService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Verifies project visibility and protected member lookup.
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class ProjectControllerCoverageTest {
    @Mock
    private ProjectService service;
    @Mock
    private ProjectAccessService access;
    @InjectMocks
    private ProjectController controller;

    @Test
    void restrictsCatalogToMembershipForOrdinaryUsers() {
        MockHttpSession session = new MockHttpSession();
        ProjectResponse project = new ProjectResponse(3L, "Project", null, 0);
        when(access.getUserId(session)).thenReturn(7L);
        when(service.getProjectsByUser(7L)).thenReturn(List.of(project));
        assertEquals(List.of(project), controller.getProjects(session));
        verify(service, never()).getProjects();
    }

    @Test
    void checksAccessBeforeReadingProjectAndMembers() {
        MockHttpSession session = new MockHttpSession();
        ProjectResponse project = new ProjectResponse(3L, "Project", null, 0);
        when(service.getProject(3L)).thenReturn(project);
        when(service.getMemberIds(3L)).thenReturn(List.of(7L));
        assertSame(project, controller.getProject(3L, session));
        assertEquals(List.of(7L), controller.getMembersIds(3L, session));
        var ordered = inOrder(access, service);
        ordered.verify(access).checkAccess(3L, session);
        ordered.verify(service).getProject(3L);
        ordered.verify(access).checkAccess(3L, session);
        ordered.verify(service).getMemberIds(3L);
    }

    @Test
    void rejectsForbiddenReadWithoutCallingProjectService() {
        MockHttpSession session = new MockHttpSession();
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(access).checkAccess(3L, session);
        assertThrows(ResponseStatusException.class, () -> controller.getProject(3L, session));
        assertThrows(ResponseStatusException.class, () -> controller.getMembersIds(3L, session));
        verifyNoInteractions(service);
    }

    @Test
    void delegatesProjectWrites() {
        ProjectRequest request = new ProjectRequest("Project", "Description", List.of(7L));
        ProjectResponse response = new ProjectResponse(3L, "Project", "Description", 0);
        when(service.create(request)).thenReturn(response);
        when(service.update(3L, request)).thenReturn(response);
        assertSame(response, controller.create(request));
        assertSame(response, controller.update(3L, request));
        controller.deleteProject(3L);
        verify(service).delete(3L);
    }
}
