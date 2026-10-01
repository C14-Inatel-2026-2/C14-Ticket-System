package com.ibm.tickets.controller.Web;

import org.springframework.stereotype.Controller;
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
    public String showCreateForm() {
        return "new-ticket";
    }

    @GetMapping
    public String showTicketList(){
        return "home";
    }
}
