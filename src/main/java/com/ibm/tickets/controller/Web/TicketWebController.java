package com.ibm.tickets.controller.Web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.service.TicketService;


/**
 * @author Rodrigo Fraga da Costa
 * TicketWebController
 */
@Controller
@RequestMapping("/tickets")
public class TicketWebController {

    private final TicketService ticketService;

    public TicketWebController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "new-ticket";
    }

    @GetMapping
    public String showTicketList(){
        return "home";
    }

    @GetMapping("/{id:\\d+}")
    public String showTicketDetails(@PathVariable Long id, Model model){
        TicketResponse ticketResponse = ticketService.findById(id);
        model.addAttribute("ticket", ticketResponse);
        return "ticket-details";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model){
        TicketResponse ticketResponse = ticketService.findById(id);
        model.addAttribute("ticket", ticketResponse);
        return "ticket-edit";
    }
    
}
