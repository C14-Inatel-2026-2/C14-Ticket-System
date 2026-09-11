package com.ibm.tickets.repository;

import org.springframework.stereotype.Repository;
import java.util.List;

import com.ibm.tickets.model.Ticket;

import java.io.IOException;
import java.io.InputStream;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

/**
 * Ticket
 * @author Mariana Barude Pina
 */

@Repository
public class TicketRepository {

    public List<Ticket> getAllTickets() throws IOException{
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/tickets.json");
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(inputStream, new TypeReference<List<Ticket>>() {});
    }

}
