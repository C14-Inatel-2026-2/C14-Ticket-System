package com.ibm.tickets.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc 
public class UserControllerTest {
 
    @Autowired 
    private MockMvc mockMvc;

    private MockHttpSession authenticatedSession(){
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedUser", "admin");
        return session;
    }

    @Test 
    void shouldReturnUnauthorizedWithoutSession() throws Exception {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedForActivityWithoutSession() throws Exception {
        mockMvc.perform(get("/api/users/me/activity"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRedirectToLoginWhenAccessingProfileWithoutSession() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void shouldOpenProfilePageWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/users/me")
                        .session(authenticatedSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile"));
    }
}
