package com.ibm.tickets.repository;

import com.ibm.tickets.model.Ticket;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TicketProjectRelationshipTest {
    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void loadsProjectReferenceFromForeignKey() {
        jdbc.update("insert into projects (name) values (?)", "Ticket System");
        Long projectId = jdbc.queryForObject(
                "select id from projects where name = ?", Long.class, "Ticket System");
        jdbc.update("""
                insert into tickets (title, description, project, project_id, assignee)
                values (?, ?, ?, ?, ?)
                """, "Linked ticket", "Description", "Ticket System", projectId, "Owner");
        Long ticketId = jdbc.queryForObject(
                "select id from tickets where title = ?", Long.class, "Linked ticket");
        entityManager.clear();

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        assertEquals(projectId, ticket.getProjectReference().getId());
        assertEquals("Ticket System", ticket.getProjectReference().getName());
        assertEquals("Ticket System", ticket.getProject());
    }
}
