package com.ibm.tickets.controller.Api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.tickets.dto.TicketActivityResponse;
import com.ibm.tickets.service.TicketActivityService;

/**
 * @author Rodrigo Fraga da Costa
 * TicketActivityController
 */
@RestController 
@RequestMapping("/api/tickets/{ticketId}/activities")
public class TicketActivityController {
    
    private final TicketActivityService activityService;

    public TicketActivityController(TicketActivityService activityService){
        this.activityService = activityService;
    }

    @GetMapping 
    public List<TicketActivityResponse> findAll(
        @PathVariable Long ticketId ) {
            return activityService.findByTicketId(ticketId);
        }
}
