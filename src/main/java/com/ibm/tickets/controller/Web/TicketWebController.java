package com.ibm.tickets.controller.Web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.tickets.dto.TicketResponse;

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

    @GetMapping
    public String showTicketList(){
        return "home";
    }
}
