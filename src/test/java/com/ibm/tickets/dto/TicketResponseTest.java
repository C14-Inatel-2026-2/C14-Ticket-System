package com.ibm.tickets.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;

class TicketResponseTest {

    @Test
    void shouldReturnProjectIdAndName() {
        Project project = new Project("Internal Systems", "Test project");

        org.springframework.test.util.ReflectionTestUtils.setField(
                project, "id", 1L);

        Ticket ticket = new Ticket("Test Ticket", "Test Description", TicketPriority.HIGH);
        ticket.setProjectReference(project);

        TicketResponse response = TicketResponse.from(ticket);

        assertEquals(1L, response.projectId());
        assertEquals("Internal Systems", response.project());
    }

    @Test
    void shouldReturnNullProjectIdWhenProjectIsNotAssociated() {
        Ticket ticket = new Ticket("Test Ticket", "Test Description", TicketPriority.HIGH);
        ticket.setProject("Legacy Project");

        TicketResponse response = TicketResponse.from(ticket);

        assertNull(response.projectId());
        assertEquals("Legacy Project", response.project());
    }
}
