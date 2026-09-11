package com.ibm.tickets.repository;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.ibm.tickets.model.Ticket;

public class TicketRepositoryTest {
    
    @Test
    void returnAllTickets() throws IOException{
        TicketRepository repository = new TicketRepository();
        List<Ticket> tickets = repository.getAllTickets();

        assertEquals(3, tickets.size());
    }
}
