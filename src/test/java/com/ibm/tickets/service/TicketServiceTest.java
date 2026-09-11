package com.ibm.tickets.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.ibm.tickets.model.Priority;
import com.ibm.tickets.model.Status;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.repository.TicketRepository;

public class TicketServiceTest {

    class TicketRepositoryMock extends TicketRepository{
        @Override
        public List<Ticket> getAllTickets() throws IOException{
            Ticket ticket1 = new Ticket(1, "Ticket de teste 1", "Projeto A", "Mariana",
            Status.OPEN, Priority.HIGH);

            Ticket ticket2 = new Ticket(2, "Ticket de teste 2", "Projeto B", "Guima",
            Status.IN_PROGRESS, Priority.MEDIUM);

            List<Ticket> tickets = new ArrayList<>();
            tickets.add(ticket1);
            tickets.add(ticket2);
            return tickets;
        }
    }
    @Test
    void returnAllTickets() throws IOException{
        TicketRepositoryMock mock = new TicketRepositoryMock();
        TicketService service = new TicketService(mock);

        List<Ticket> tickets = service.getAllTickets();
        assertEquals("Ticket de teste 1", tickets.get(0).getName());
        assertEquals("Ticket de teste 2", tickets.get(1).getName());
    }
}
