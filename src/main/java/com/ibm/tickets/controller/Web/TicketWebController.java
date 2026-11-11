package com.ibm.tickets.controller.Web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.service.TicketService;


/**
 * @author Rodrigo Fraga da Costa
 * TicketWebController
 */
@Controller
@RequestMapping("project/{projectId}/tickets")
public class TicketWebController {

    private final TicketService ticketService;

    public TicketWebController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long projectId) {
        return "new-ticket";
    }

    @GetMapping
    public String showTicketList(@PathVariable Long projectId){
        return "home";
    }

    @GetMapping("/{ticketId:\\d+}")
    public String showTicketDetails(@PathVariable Long projectId, @PathVariable Long ticketId, Model model){
        TicketResponse ticketResponse = findTicketInProject(projectId, ticketId);
        model.addAttribute("ticket", ticketResponse);
        return "ticket-details";
    }

    @GetMapping("/{ticketId}/edit")
    public String showEditForm(@PathVariable Long projectId, @PathVariable Long ticketId, Model model){
        TicketResponse ticketResponse = findTicketInProject(projectId, ticketId);
        model.addAttribute("ticket", ticketResponse);
        return "ticket-edit";
    }

    private TicketResponse findTicketInProject(Long projectId, Long ticketId) {

        TicketResponse ticket = ticketService.findById(ticketId);

        if (ticket.projectId() == null || !ticket.projectId().equals(projectId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Ticket not found in this project");
        }

        return ticket;
    }
    
}
