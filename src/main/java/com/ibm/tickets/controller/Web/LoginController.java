package com.ibm.tickets.controller.Web;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.tickets.model.User;
import com.ibm.tickets.service.AuthenticationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * @author Rodrigo Fraga da Costa
 * LoginController
 */
@Controller 
public class LoginController {

    private final AuthenticationService authenticationService;

    @Value("${c14.dev-admin.enabled:false}")
    private boolean devAdminEnabled;
    
    public LoginController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                        @RequestParam String password,
                        HttpServletRequest request) {

        boolean emailEmpty = email == null || email.isBlank();
        boolean passwordEmpty = password == null || password.isBlank();

        if (emailEmpty && passwordEmpty) {
            return "redirect:/?error=email&error=password";
        }

        if (emailEmpty) {
            return "redirect:/?error=email";
        }

        if (passwordEmpty) {
            return "redirect:/?error=password";
        }

        // Global development administrator
        if ("admin".equals(email) && "admin".equals(password)) {
            HttpSession session = createLoginSession(request);

            session.removeAttribute("userId");
            session.setAttribute("loggedUser", "admin");
            session.setAttribute("userRole", "ADMIN");
            session.setAttribute("globalAdmin", true);

            return "redirect:/project";
        }

        // Database authentication
        Optional<User> authenticatedUser =
                authenticationService.authenticate(
                        email,
                        password
                );

        if (authenticatedUser.isEmpty()) {
            return "redirect:/?error";
        }

        User user = authenticatedUser.get();

        HttpSession session = createLoginSession(request);

        session.setAttribute("userId", user.getId());
        session.setAttribute("loggedUser", user.getName());
        session.setAttribute("userRole", user.getRole().name());
        session.removeAttribute("globalAdmin");

        return "redirect:/project";
    }

    private HttpSession createLoginSession(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session != null) {
            request.changeSessionId();
            return session;
        }

        return request.getSession(true);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if(session != null) session.invalidate();

        return "redirect:/";
    }

}
