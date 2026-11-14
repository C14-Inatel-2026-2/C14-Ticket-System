package com.ibm.tickets.controller.Api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.dto.TicketStatusRequest;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.service.TicketService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

/**
 * Provides REST endpoints for ticket management.
 *
 * @author Gabriel Guimaraes
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(
            @Valid @RequestBody TicketRequest request) {
        return ticketService.create(request);
    }

    @GetMapping
    public List<TicketResponse> findAll(
        @RequestParam(required = false) Long projectId,
        @RequestParam(required = false) TicketStatus status,
        @RequestParam(required = false) TicketPriority priority) {

        return ticketService.findAll(projectId, status, priority);
    }

    @GetMapping("/{id}")
    public TicketResponse findById(@PathVariable Long id, @RequestParam Long projectId) {
        return ticketService.findByIdInProject(id, projectId);
    }

    @PutMapping("/{id}")
    public TicketResponse update(
            @PathVariable Long id,
            @RequestParam Long projectId,
            @Valid @RequestBody TicketRequest request,
            HttpSession session) {
        
        ticketService.findByIdInProject(id, projectId);

       if (!request.projectId().equals(projectId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Ticket not found in this project");
        }
        return ticketService.update(id, request, getAuthor(session));
    }

    @PatchMapping("/{id}/status")
    public TicketResponse updateStatus(
            @PathVariable Long id,
            @RequestParam Long projectId,
            @Valid @RequestBody TicketStatusRequest request,
            HttpSession session) {

        ticketService.findByIdInProject(id, projectId);
        return ticketService.updateStatus(id, request.status(), getAuthor(session));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @RequestParam Long projectId) {
        
        ticketService.findByIdInProject(id, projectId);
        ticketService.delete(id);
    }

    public String getAuthor(HttpSession session){
        Object user = session.getAttribute("loggedUser");
        if(user == null){
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "User not authenticated"
            );
        }
        return user.toString();
    }
}