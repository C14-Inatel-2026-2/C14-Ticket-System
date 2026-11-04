package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.service.ProjectService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the public contract of the project catalog endpoint.
 * @author Gabriel Guimaraes
 */
@WebMvcTest(ProjectController.class)
class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("loggedUser", "admin");
    }

    @Test
    void returnsPersistedProjectCatalog() throws Exception {
        when(projectService.getProjects()).thenReturn(List.of(
                new ProjectResponse(42L, "Persisted", "From database", 7)));

        mockMvc.perform(get("/api/projects").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].length()").value(4))
                .andExpect(jsonPath("$[0].id").value(42))
                .andExpect(jsonPath("$[0].name").value("Persisted"))
                .andExpect(jsonPath("$[0].description").value("From database"))
                .andExpect(jsonPath("$[0].ticketCount").value(7));
        verify(projectService).getProjects();
    }

    @Test
    void returnsEmptyArrayWhenThereAreNoProjects() throws Exception {
        when(projectService.getProjects()).thenReturn(List.of());

        mockMvc.perform(get("/api/projects").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
        verify(projectService).getProjects();
    }
}
