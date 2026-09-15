package com.ibm.tickets.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.service.TicketService;

/**
 * @author Rodrigo Fraga da Costa
 * @author Mariana Barude Pina
 * Home
 */
@Controller
@RequestMapping("/")
public class Home {
    
    private final TicketService ticketService;
    
    public Home(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    @GetMapping
    public String homePage(Model model){
        List<TicketResponse> tickets = ticketService.findAll(null);

        model.addAttribute("tickets", tickets);
        
        return "home";
    }
}
