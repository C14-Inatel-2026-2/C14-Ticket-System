package com.ibm.tickets.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.service.TicketService;

/**
 * Tests the REST endpoints provided by TicketController.
 *
 * @author Gabriel Guimaraes
 */
@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void shouldCreateTicket() throws Exception {
        OffsetDateTime now = OffsetDateTime.parse(
                "2026-09-18T15:00:00Z");

        TicketResponse response = new TicketResponse(
                1L,
                "Printer problem",
                "The printer is not working",
                "Internal Systems",
                "Gabriel",
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                now,
                now);

        when(ticketService.create(any(TicketRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Printer problem",
                          "description": "The printer is not working",
                          "project": "Internal Systems",
                          "assignee": "Gabriel",
                          "priority": "HIGH"
                        }
                        """))
                .andExpect(status().isCreated())
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
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "",
                          "description": "The printer is not working",
                          "project": "Internal Systems",
                          "assignee": "Gabriel",
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
                "Internal Systems",
                "Gabriel",
                TicketStatus.OPEN,
                TicketPriority.HIGH,
                now,
                now);

        when(ticketService.findAll(null))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/tickets"))
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
                "Internal Systems",
                "Gabriel",
                TicketStatus.IN_PROGRESS,
                TicketPriority.HIGH,
                now,
                now);

        when(ticketService.updateStatus(
                1L,
                TicketStatus.IN_PROGRESS))
                .thenReturn(response);

        mockMvc.perform(patch("/api/tickets/1/status")
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

        when(ticketService.findById(99L))
                .thenThrow(new TicketNotFoundException(99L));

        mockMvc.perform(get("/api/tickets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title")
                        .value("Ticket not found"))
                .andExpect(jsonPath("$.status")
                        .value(404));
    }
}