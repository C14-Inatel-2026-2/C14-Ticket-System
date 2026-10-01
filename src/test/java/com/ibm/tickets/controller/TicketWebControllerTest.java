package com.ibm.tickets.controller;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.ibm.tickets.controller.Web.TicketWebController;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.service.TicketService;

/**
 * @author Rodrigo Fraga da Costa
 * TicketWebControllerTest
 */
@WebMvcTest(TicketWebController.class) 
public class TicketWebControllerTest {
    
    @Autowired 
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    private MockHttpSession session;

    @BeforeEach 
    void setUp(){
        session = new MockHttpSession();
        session.setAttribute("loggedUser", "admin");
    }

    @Test 
    void shouldOpenNewTicketPage() throws Exception{
        mockMvc.perform(get("/tickets/new")
                .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("new-ticket"));
    }

    @Test 
    void shouldReturnNotFoundForInvalidTicketPage() throws Exception{
        mockMvc.perform(get("/tickets/invalid")
                .session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnTicketHomePage() throws Exception{
        mockMvc.perform(get("/tickets").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("home"));
    }

    @Test
    void shouldShowTicketDetails() throws Exception{
        TicketResponse ticketResponse = new TicketResponse(
            4L,
            "Erro ao realizar login",
            "Usuario nao consegue realizar login",
            "Ticket System",
            "Mariana",
            TicketStatus.OPEN,
            TicketPriority.HIGH,
            OffsetDateTime.now(),
            OffsetDateTime.now()
        );
        when(ticketService.findById(4L)).thenReturn(ticketResponse);

        mockMvc.perform(get("/tickets/4").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("ticket-details"));
    }

    @Test
    void shouldShowEditForm() throws Exception{
        TicketResponse ticketResponse = new TicketResponse(
            4L,
            "Erro ao realizar login",
            "Usuario nao consegue realizar login",
            "Projeto de Teste",
            "Mariana",
            TicketStatus.OPEN,
            TicketPriority.HIGH,
            OffsetDateTime.now(),
            OffsetDateTime.now());

        when(ticketService.findById(4L)).thenReturn(ticketResponse);

        mockMvc.perform(get("/tickets/4/edit").session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("ticket-edit"))
        .andExpect(model().attribute("ticket", ticketResponse));
    }
}
