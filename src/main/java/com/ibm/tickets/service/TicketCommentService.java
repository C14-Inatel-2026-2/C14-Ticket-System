package com.ibm.tickets.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ibm.tickets.repository.TicketCommentRepository;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.dto.TicketCommentRequest;
import com.ibm.tickets.dto.TicketCommentResponse;
import com.ibm.tickets.model.TicketComment;
import com.ibm.tickets.model.Ticket;

import org.springframework.transaction.annotation.Transactional;

/**
 * TicketCommentService
 * @author Rodrigo Costa
 */
@Service 
public class TicketCommentService {
    
    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public TicketCommentService(TicketCommentRepository commentRepository, 
        TicketRepository ticketRepository){
            this.commentRepository = commentRepository;
            this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> findByTicketId(Long ticketId){
        if(!ticketRepository.existsById(ticketId)){
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Ticket not found"
            );
        }

        return commentRepository
            .findAllByTicketIdOrderByCreatedAtDesc(ticketId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional 
    public TicketCommentResponse create(
        Long ticketId,
        String author,
        TicketCommentRequest request){

            Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> 
                new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND,
                "Ticket not found"
                )
            );  

            TicketComment comment = new TicketComment(
                ticket, 
                author, 
            request.content().trim()
        );

        return toResponse(commentRepository.save(comment));
    }

    private TicketCommentResponse toResponse(TicketComment comment){
        return new TicketCommentResponse(
            comment.getId(), 
            comment.getAuthor(), 
            comment.getContent(), 
            comment.getCreatedAt()
        );
    }
}
