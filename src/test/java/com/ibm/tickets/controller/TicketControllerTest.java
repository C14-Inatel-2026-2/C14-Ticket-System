package com.ibm.tickets.controller;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
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
}