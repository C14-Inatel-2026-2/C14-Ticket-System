
package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.ProjectRequest;
import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.service.ProjectAccessService;
import com.ibm.tickets.service.ProjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectAccessService projectAccessService;


    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("loggedUser", "admin");
        session.setAttribute("userRole", UserRole.ADMIN.name());
    }

    @Test
    void shouldCreateProject() throws Exception {

        ProjectResponse response = new ProjectResponse(
                1L,
                "New Project",
                "Project description",
                0
        );

        when(projectService.create(any(ProjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/projects")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "New Project",
                            "description": "Project description",
                            "membersIds": [1, 2]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description")
                        .value("Project description"))
                .andExpect(jsonPath("$.ticketCount").value(0));

        verify(projectService).create(any(ProjectRequest.class));
    }

    @Test
    void shouldFindProjectById() throws Exception {

        ProjectResponse response = new ProjectResponse(
                1L,
                "C14 Ticket System",
                "Ticket management system",
                5
        );

        when(projectService.getProject(1L))
                .thenReturn(response);

        mockMvc.perform(get("/api/projects/1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("C14 Ticket System"))
                .andExpect(jsonPath("$.ticketCount").value(5));

        verify(projectService).getProject(1L);
    }

    @Test
    void shouldUpdateProject() throws Exception {

        ProjectResponse response = new ProjectResponse(
                1L,
                "Updated Project",
                "Updated description",
                3
        );

        when(projectService.update(
                eq(1L),
                any(ProjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/projects/1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Updated Project",
                            "description": "Updated description",
                            "membersIds": [1, 3]
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Updated Project"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"))
                .andExpect(jsonPath("$.ticketCount").value(3));

        verify(projectService).update(
                eq(1L),
                any(ProjectRequest.class));
    }

    @Test
    void shouldDeleteProject() throws Exception {

        mockMvc.perform(delete("/api/projects/1")
                .session(session))
                .andExpect(status().isNoContent());

        verify(projectService).delete(1L);
    }

    @Test
    void shouldRejectProjectWithEmptyName() throws Exception {

        mockMvc.perform(post("/api/projects")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "",
                            "description": "Invalid project",
                            "membersIds": [1, 2]
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(projectService);
    }
}
