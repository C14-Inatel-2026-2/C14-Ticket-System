package com.ibm.tickets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.repository.TicketRepository;

/**
 * Provides the business operations for tickets.
 *
 * @author Gabriel Guimaraes
 */
@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public TicketResponse create(TicketRequest request) {
        Ticket ticket = new Ticket(
                request.title(),
                request.description(),
                request.priority());
        
        ticket.setProject(request.project());
        ticket.setAssignee(request.assignee());
        
        Ticket savedTicket = ticketRepository.saveAndFlush(ticket);
        return TicketResponse.from(savedTicket);
    }

    public List<TicketResponse> findAll(TicketStatus status) {
        List<Ticket> tickets;

        if (status == null) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else {
            tickets = ticketRepository
                    .findAllByStatusOrderByCreatedAtDesc(status);
        }

        return tickets.stream()
                .map(TicketResponse::from)
                .toList();
    }

    public TicketResponse findById(Long id) {
        return TicketResponse.from(findEntityById(id));
    }

    @Transactional
    public TicketResponse update(Long id, TicketRequest request) {
        Ticket ticket = findEntityById(id);

        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setProject(request.project());
        ticket.setAssignee(request.assignee());
        ticket.setPriority(request.priority());

        Ticket updatedTicket = ticketRepository.saveAndFlush(ticket);
        return TicketResponse.from(updatedTicket);
    }

    @Transactional
    public TicketResponse updateStatus(
            Long id,
            TicketStatus status) {
        Ticket ticket = findEntityById(id);
        ticket.setStatus(status);

        Ticket updatedTicket = ticketRepository.saveAndFlush(ticket);
        return TicketResponse.from(updatedTicket);
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findEntityById(id);
        ticketRepository.delete(ticket);
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }
}