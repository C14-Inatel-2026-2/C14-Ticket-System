package com.ibm.tickets.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.ibm.tickets.MockedClasses.HttpSessionClassMock;
import com.ibm.tickets.controller.Web.LoginController;

import jakarta.servlet.http.HttpSession;

public class LoginControllerTest {
    
    @Mock 
    HttpSession session;

    @Test 
    void shouldReturnLoginPage(){
        assertEquals("login", new Home().homePage());
    }

    @Test 
    void shouldRedirectToTicketsPageOnSuccessfulLogin(){
        HttpSession session = (HttpSession) new HttpSessionClassMock();
        LoginController loginController = new LoginController();
        
        String result =loginController.login("admin", "admin", session);
        assertEquals("redirect:/tickets", result);
    }

    @Test 
    void shouldRedirectToLoginPageOnFailedLogin(){
        LoginController loginController = new LoginController();
        String result =loginController.login("wrongUser", "wrongPass", session);
        assertEquals("redirect:/?error", result);
    }
}
