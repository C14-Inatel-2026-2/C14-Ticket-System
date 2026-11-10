package com.ibm.tickets.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.tickets.model.TicketActivity;

/**
 * @author Rodrigo Fraga da Costa
 * TicketActivityRepository
 */
public interface TicketActivityRepository 
        extends JpaRepository<TicketActivity, Long>{
    
            List<TicketActivity> findAllByTicketIdOrderByCreatedAtAsc(
                Long ticketId
            );
}
