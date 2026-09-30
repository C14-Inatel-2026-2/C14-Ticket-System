package com.ibm.tickets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Rodrigo Fraga da Costa
 * TicketWebController
 */
@Controller
@RequestMapping("/tickets")
public class TicketWebController {
    
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        return "new-ticket";
    }
}
