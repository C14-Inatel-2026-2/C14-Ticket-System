package com.ibm.tickets.controller.Web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.service.ProjectService;
import com.ibm.tickets.service.TicketService;
import com.ibm.tickets.service.UserService;


/**
 * @author Rodrigo Fraga da Costa
 * TicketWebController
 */
@Controller
@RequestMapping("project/{projectId}/tickets")
public class TicketWebController {

    private final TicketService ticketService;
    private final UserService userService;
    private final ProjectService projectService;

    public TicketWebController(TicketService ticketService, UserService userService, ProjectService projectService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.projectService = projectService;
    }

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long projectId, Model model) {
        List<Long> projectMembersIds = projectService.getMemberIds(projectId);
        model.addAttribute("users", userService.getAllUsersByIds(projectMembersIds));
        return "new-ticket";
    }

    @GetMapping
    public String showTicketList(@PathVariable Long projectId, Model model){
        model.addAttribute("project", projectService.getProject(projectId));
        return "home";
    }

    @GetMapping("/{ticketId:\\d+}")
    public String showTicketDetails(@PathVariable Long projectId, @PathVariable Long ticketId, Model model){
        TicketResponse ticketResponse = ticketService.findByIdInProject(ticketId, projectId);
        model.addAttribute("ticket", ticketResponse);
        return "ticket-details";
    }

    @GetMapping("/{ticketId}/edit")
    public String showEditForm(@PathVariable Long projectId, @PathVariable Long ticketId, Model model){
        TicketResponse ticketResponse = ticketService.findByIdInProject(ticketId, projectId);
        model.addAttribute("ticket", ticketResponse);

        List<Long> projectMembersIds = projectService.getMemberIds(projectId);
        
        model.addAttribute("users", userService.getAllUsersByIds(projectMembersIds));
        return "ticket-edit";
    }
}
