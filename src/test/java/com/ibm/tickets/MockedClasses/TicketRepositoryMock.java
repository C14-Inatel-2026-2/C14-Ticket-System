package com.ibm.tickets.MockedClasses;

import java.util.List;
import java.util.Optional;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketStatus;

public interface TicketRepositoryMock{

    Ticket save(Ticket entity);
    List<Ticket> findAllByOrderByCreatedAtDesc();
    List<Ticket> findAllByStatusOrderByCreatedAtDesc(TicketStatus status);
    Optional<Ticket> findById(Long id);
    void deleteById(Long id);
}
