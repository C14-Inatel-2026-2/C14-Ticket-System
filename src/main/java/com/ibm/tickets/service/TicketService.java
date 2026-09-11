package com.ibm.tickets.service;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.repository.TicketRepository;

/**
 * Ticket Service
 * @author Mariana Barude Pina
 */

@Service
public class TicketService {
    private TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    public List<Ticket> getAllTickets() throws IOException{
        return repository.getAllTickets();
    }
}
