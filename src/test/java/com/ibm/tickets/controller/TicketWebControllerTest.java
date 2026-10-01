package com.ibm.tickets.controller;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.ibm.tickets.controller.Web.TicketWebController;

/**
 * @author Rodrigo Fraga da Costa
 * TicketWebControllerTest
 */
@WebMvcTest(TicketWebController.class) 
public class TicketWebControllerTest {
    
    @Autowired 
    private MockMvc mockMvc;

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
                .andExpect(view().name("new-ticket"));
    }

    @Test 
    void shouldReturnNotFoundForInvalidTicketPage() throws Exception{
        mockMvc.perform(get("/tickets/invalid")
                .session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnTicketHomePage(){
        assertEquals("home", new TicketWebController().showTicketList());
    }

    @Test 
    void shouldReturnNewTicketPage(){
        assertEquals("new-ticket", new TicketWebController().showCreateForm());
    }
}
