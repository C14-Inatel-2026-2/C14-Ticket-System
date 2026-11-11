package com.ibm.tickets.service;

import com.ibm.tickets.repository.TicketCommentRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.tickets.dto.TicketActivityResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketActivity;
import com.ibm.tickets.repository.TicketActivityRepository;
import com.ibm.tickets.repository.TicketRepository;

/**
 * @author Rodrigo Fraga da Costa
 * TicketActivityService
 */
@Service 
public class TicketActivityService {
    
    private final TicketActivityRepository activityRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public TicketActivityService(
            TicketActivityRepository activityRepository,
            TicketCommentRepository commentRepository,
            TicketRepository ticketRepository) {
        this.activityRepository = activityRepository;
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }

    public void record(
            Ticket ticket,
            String author,
            String type,
            String oldValue,
            String newValue
    ){
        if(Objects.equals(oldValue, newValue)) return;
        activityRepository.save(new TicketActivity(
            ticket,
            author,
            type,
            oldValue,
            newValue
        ));
    }

    @Transactional(readOnly = true)
    public List<TicketActivityResponse> findByTicketId(Long ticketId){
        if(!ticketRepository.existsById(ticketId)){
            throw new TicketNotFoundException(ticketId);
        }

        List<TicketActivityResponse> result = new ArrayList<>();

        activityRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId)
            .forEach(a -> result.add(new TicketActivityResponse(
                "history",
                a.getAuthor(),
                a.getActivityType(),
                a.getOldValue(),
                a.getNewValue(),
                null,
                a.getCreatedAt()
            )));;
        
        commentRepository.findAllByTicketIdOrderByCreatedAtDesc(ticketId)
            .forEach(c -> result.add(new TicketActivityResponse(
                    "comment",
                    c.getAuthor(),
                    "COMMENT",
                    null,
                    null,
                    c.getContent(),
                    c.getCreatedAt()
                )));

        result.sort(Comparator.comparing(
           TicketActivityResponse::createdAt,
           Comparator.nullsLast(Comparator.naturalOrder()) 
        ));

        return result;
    }
}
