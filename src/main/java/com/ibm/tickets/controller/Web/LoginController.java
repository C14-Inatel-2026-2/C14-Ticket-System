package com.ibm.tickets.controller.Web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/**
 * @author Rodrigo Fraga da Costa
 * LoginController
 */
@Controller 
public class LoginController {
    
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    @PostMapping("/login")
    public String login(@RequestParam String username, 
                        @RequestParam String password,
                        Model model,
                        HttpSession session) 
    {
        if (USERNAME.equals(username) && PASSWORD.equals(password)) {
            session.setAttribute("loggedUser", username);
            return "redirect:/tickets";
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
