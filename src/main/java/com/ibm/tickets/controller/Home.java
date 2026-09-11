package com.ibm.tickets.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.service.TicketService;

/**
 * @author Rodrigo Fraga da Costa
 * Home
 */
@Controller
@RequestMapping("/")
public class Home {
    
    private TicketService ticketService;
    
    public Home(TicketService ticketService) {
        this.ticketService = ticketService;
    }
    
    @GetMapping
    public String homePage(Model model) throws IOException{
        List<Ticket> tickets = ticketService.getAllTickets();

        model.addAttribute("tickets", tickets);
        
        return "home";
    }
}
