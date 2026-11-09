package com.ibm.tickets.controller.Api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.tickets.service.TicketCommentService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.ibm.tickets.dto.TicketCommentRequest;
import com.ibm.tickets.dto.TicketCommentResponse;

/**
 * @author Rodrigo Costa
 * TicketCommentController
 */
@RestController 
@RequestMapping("/api/tickets/{ticketId}/comments") 
public class TicketCommentController {
    
    private final TicketCommentService ticketCommentService;

    public TicketCommentController(TicketCommentService ticketCommentService){
        this.ticketCommentService = ticketCommentService;
    }

    @GetMapping 
    public List<TicketCommentResponse> findAll(@PathVariable Long ticketId){
        return ticketCommentService.findByTicketId(ticketId);
    }

    @PostMapping 
    @ResponseStatus(HttpStatus.CREATED)
    public TicketCommentResponse create(
        @PathVariable Long ticketId,
        @Valid @RequestBody TicketCommentRequest request,
        HttpSession session
    ){
        Object loggedUser = session.getAttribute("loggedUser");

        if(loggedUser == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not authenticated"
            );
        }

        return ticketCommentService.create(ticketId, loggedUser.toString(), request);
    }
}
