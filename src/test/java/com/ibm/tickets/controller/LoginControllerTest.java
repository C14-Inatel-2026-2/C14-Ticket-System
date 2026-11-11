package com.ibm.tickets.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;

import com.ibm.tickets.MockedClasses.HttpSessionClassMock;
import com.ibm.tickets.controller.Web.LoginController;
import com.ibm.tickets.model.User;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.service.AuthenticationService;

import jakarta.servlet.http.HttpSession;

public class LoginControllerTest {
    
    private AuthenticationService authenticationService;
    private LoginController loginController;
    private MockHttpServletRequest request;

    @Mock 
    HttpSession session;

    @BeforeEach 
    void setUp(){
        authenticationService = Mockito.mock(AuthenticationService.class);
        loginController = new LoginController(authenticationService);
        request = new MockHttpServletRequest();
    }

    @Test 
    void shouldLoginWithPersistedUser(){
        User user = new User("user1", "user1@email.com", UserRole.ADMIN);
        when(authenticationService.authenticate
                ("user1@email.com", "password"))
                    .thenReturn(Optional.of(user));
        String result = loginController.login("user1@email.com",
                     "password", request);
        assertEquals("redirect:/project", result);
        assertNotNull(request.getSession(false));

        assertEquals("user1", request.getSession(false).getAttribute("loggedUser"));
        verify(authenticationService).authenticate
            ("user1@email.com", "password");
    }

    @Test 
    void shouldReturnLoginPage(){
        assertEquals("login", new Home().homePage());
    }

    @Test
    void shouldRejectInvalidCredentials() {

        when(authenticationService.authenticate(
                "invalid@example.com",
                "wrong"
        )).thenReturn(Optional.empty());

        String result = loginController.login(
                "invalid@example.com",
                "wrong",
                request
        );

        assertEquals("redirect:/?error", result);
        assertNull(request.getSession(false));
    }

    @Test
    void shouldRejectEmptyEmail() {

        String result = loginController.login(
                "",
                "password",
                request
        );

        assertEquals(
                "redirect:/?error=email",
                result
        );
    }

     @Test
    void shouldRejectEmptyPassword() {

        String result = loginController.login(
                "admin@example.com",
                "",
                request
        );

        assertEquals(
                "redirect:/?error=password",
                result
        );
    }
}
