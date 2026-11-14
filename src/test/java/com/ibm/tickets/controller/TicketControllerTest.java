package com.ibm.tickets.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ibm.tickets.controller.Api.TicketController;
import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.exception.TicketNotInProjectException;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.service.ProjectAccessService;
import com.ibm.tickets.service.TicketService;

import jakarta.servlet.http.HttpSession;

/**
 * Tests the REST endpoints provided by TicketController.
 *
 * @author Gabriel Guimaraes
 */
@WebMvcTest(TicketController.class)
class TicketControllerTest {


    @MockitoBean
    private ProjectAccessService projectAccessService;
    
    static Stream<Arguments> invalidCreateFields() {
        return Stream.of(
                Arguments.of("assignee", "null"), Arguments.of("assignee", "\"\""),
                Arguments.of("assignee", "\"   \""),
                Arguments.of("assignee", "\"" + "a".repeat(101) + "\""),
                Arguments.of("title", "\"" + "t".repeat(151) + "\""),
                Arguments.of("projectId", "null"),
                Arguments.of("description", "\"   \""),
                Arguments.of("priority", "null"));
    }

    @ParameterizedTest
    @MethodSource("invalidCreateFields")
    void rejectsInvalidCreateBeforeCallingService(String field, String jsonValue) throws Exception {
        String json = """
                {"title":"Title","description":"Description","projectId":1,
                 "assignee":"Owner", "assigneeUserId": 1, "priority":"HIGH"}
                """;
        json = json.replaceAll(
            "\"" + field + "\":(?:\"[^\"]*\"|null|\\d+)",
            java.util.regex.Matcher.quoteReplacement("\"" + field + "\":" + jsonValue));

        mockMvc.perform(post("/api/tickets").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + field).isString());
        verifyNoInteractions(ticketService);
    }

    @Test
    void rejectsCreateWithoutSessionBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title":"Title","description":"Description","projectId":1,
                         "assignee":"Owner", "assigneeUserId": 1, "priority":"HIGH"}
                        """))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(ticketService);
    }

    @Test
    void rejectsUnknownPriorityBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/tickets").session(session)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"title":"Title","description":"Description","projectId":1,
                         "assignee":"Owner","priority":"UNKNOWN"}
                        """))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(ticketService);
    }

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @MockitoBean
    private TicketService ticketService;
    

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("loggedUser", "admin");

        assertNotNull(session);
    }

    @Test
    void shouldCreateTicket() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse(
                "2026-09-18T15:00:00Z");

        TicketResponse response = new TicketResponse(
                1L,
                "Printer problem",
                "The printer is not working",
                1L,
                "Internal Systems",
                "Gabriel",
                1L,
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                now,
                now);

        when(ticketService.create(any(TicketRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/tickets")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Printer problem",
                          "description": "The printer is not working",
                          "projectId": 1,
                          "assignee": "Gabriel",
                          "assigneeUserId": 1,
                          "priority": "HIGH"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Printer problem"))
                .andExpect(jsonPath("$.projectId")
                        .value(1))
                .andExpect(jsonPath("$.project")
                        .value("Internal Systems"))
                .andExpect(jsonPath("$.assignee")
                        .value("Gabriel"))
                .andExpect(jsonPath("$.status")
                        .value("OPEN"))
                .andExpect(jsonPath("$.priority")
                        .value("HIGH"));
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "",
                          "description": "The printer is not working",
                          "projectId": 1,
                          "assignee": "Gabriel",
                          "assigneeUserId": 1,
                          "priority": "HIGH"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Validation error"))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.detail")
                        .value("One or more fields are invalid."))
                .andExpect(jsonPath("$.errors.title")
                        .value("Title is required"));
    }

    @Test
    void shouldListTickets() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse(
                "2026-09-18T15:00:00Z");

        TicketResponse response = new TicketResponse(
                1L,
                "Printer problem",
                "The printer is not working",
                1L,
                "Internal Systems",
                "Gabriel",
                1L,
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                now,
                now);

        when(ticketService.findAll(null, null, null))
                .thenReturn(List.of(response));

        
        when(projectAccessService.hasGlobalAccess(any(HttpSession.class)))
                .thenReturn(true);

        mockMvc.perform(get("/api/tickets")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Printer problem"))
                .andExpect(jsonPath("$[0].status")
                        .value("OPEN"))
                .andExpect(jsonPath("$[0].priority")
                        .value("HIGH"));
    }

    @Test
    void shouldUpdateTicketStatus() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse(
                "2026-09-18T15:00:00Z");

        TicketResponse response = new TicketResponse(
                1L,
                "Printer problem",
                "The printer is not working",
                1L,
                "Internal Systems",
                "Gabriel",
                1L,
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                now,
                now);
        when(ticketService.findByIdInProject(1L, 1L)).thenReturn(response);
        when(ticketService.updateStatus(
                1L,
                TicketStatus.IN_PROGRESS,
                "admin"))
                .thenReturn(response);

        mockMvc.perform(patch("/api/tickets/1/status?projectId=1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "IN_PROGRESS"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("IN_PROGRESS"));

    }

    @Test
    void shouldReturnNotFoundWhenTicketDoesNotExist()
            throws Exception {

        when(ticketService.findByIdInProject(99L, 1L)).thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/tickets/99?projectId=1")
                .session(session))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title")
                        .value("Ticket not found"))
                .andExpect(jsonPath("$.status")
                        .value(404));
    }

    @Test
    void shouldFindTicketById() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse(
                "2026-09-18T15:00:00Z");

        TicketResponse response = new TicketResponse(
                1L,
                "Printer problem",
                "The printer is not working",
                1L,
                "Internal Systems",
                "Gabriel",
                1L,
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                now,
                now);

        when(ticketService.findByIdInProject(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/api/tickets/1?projectId=1")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Printer problem"))
                .andExpect(jsonPath("$.project")
                        .value("Internal Systems"))
                .andExpect(jsonPath("$.assignee")
                        .value("Gabriel"))
                .andExpect(jsonPath("$.status")
                        .value("OPEN"))
                .andExpect(jsonPath("$.priority")
                        .value("HIGH"));
    }

	@Test
	void shouldReturnNotFoundWhenTicketBelongsToAnotherProject()throws Exception {
		when(ticketService.findByIdInProject(1L, 1L)).thenThrow(new TicketNotInProjectException());

		mockMvc.perform(get("/api/tickets/1?projectId=1")
				.session(session))
				.andExpect(status().isNotFound());

		verify(ticketService).findByIdInProject(1L, 1L);
	}

    @Test
    void shouldUpdateTicket() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse(
                "2026-09-18T15:00:00Z");

        TicketResponse response = new TicketResponse(
                1L,
                "Updated printer problem",
                "The printer continues to fail",
                1L,
                "Internal Systems",
                "Gabriel",
                1L,
                TicketStatus.OPEN,
                TicketPriority.URGENT,
                now,
                now);
        when(ticketService.findByIdInProject(1L, 1L)).thenReturn(response);
        when(ticketService.update(
                eq(1L),
                any(TicketRequest.class), eq("admin")))
                .thenReturn(response);

        mockMvc.perform(put("/api/tickets/1?projectId=1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Updated printer problem",
                          "description": "The printer continues to fail",
                          "projectId": 1,
                          "assignee": "Gabriel",
                          "assigneeUserId": 1,
                          "priority": "URGENT"
                        }
                        """))
                        .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Updated printer problem"))
                .andExpect(jsonPath("$.description")
                        .value("The printer continues to fail"))
                .andExpect(jsonPath("$.priority")
                        .value("URGENT"));
    }

    @Test
    void shouldDeleteTicket() throws Exception {
		OffsetDateTime now = OffsetDateTime.parse("2026-09-18T15:00:00Z");
    	TicketResponse response = new TicketResponse(
            1L,
            "Printer problem",
            "The printer is not working",
            1L,
            "Internal Systems",
            "Gabriel",
            1L,
            TicketStatus.OPEN,
            TicketPriority.HIGH,
            now,
            now);

    	when(ticketService.findByIdInProject(1L, 1L)).thenReturn(response);

        mockMvc.perform(delete("/api/tickets/1?projectId=1")
                .session(session))
                .andExpect(status().isNoContent());

        verify(ticketService).delete(1L);
    }

	@Test
	void shouldNotUpdateTicketFromAnotherProject() throws Exception {
		when(ticketService.findByIdInProject(1L, 1L)).thenThrow(new TicketNotInProjectException());

		mockMvc.perform(put("/api/tickets/1?projectId=1")
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
							"title": "Updated printer problem",
							"description": "Updated description",
							"projectId": 1,
							"assignee": "Gabriel",
                                                        "assigneeUserId": 1,
							"priority": "HIGH"
						}
						"""))
				.andExpect(status().isNotFound());

		verify(ticketService).findByIdInProject(1L, 1L);
	}

	@Test
	void shouldNotChangeStatusOfTicketFromAnotherProject()throws Exception {
		when(ticketService.findByIdInProject(1L, 1L)).thenThrow(new TicketNotInProjectException());

		mockMvc.perform(patch("/api/tickets/1/status?projectId=1")
				.session(session)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
							"status": "IN_PROGRESS"
						}
						"""))
				.andExpect(status().isNotFound());

		verify(ticketService).findByIdInProject(1L, 1L);
	}

	@Test
	void shouldNotDeleteTicketFromAnotherProject() throws Exception {
		when(ticketService.findByIdInProject(1L, 1L)).thenThrow(new TicketNotInProjectException());

		mockMvc.perform(delete("/api/tickets/1?projectId=1")
				.session(session))
				.andExpect(status().isNotFound());

		verify(ticketService).findByIdInProject(1L, 1L);
	}


    @Test
    void shouldReturnBadRequestWhenStatusIsInvalid()throws Exception {

        mockMvc.perform(patch("/api/tickets/1/status")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "status": "INVALID_STATUS"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldReturnUnauthorizedWhenAccessingTicketsWithoutSession() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }
}
