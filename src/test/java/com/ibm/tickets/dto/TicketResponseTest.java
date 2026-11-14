package com.ibm.tickets.dto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;

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

    @Test
    void shouldIdentifyOverdueOpenTicket() {
        Ticket ticket = new Ticket(
            "Overdue ticket",
            "Description",
            TicketPriority.HIGH
        );

        ticket.setDueDate(LocalDate.now().minusDays(1));

        TicketResponse response = TicketResponse.from(ticket);

        assertTrue(response.overdue());
        assertEquals(ticket.getDueDate(), response.dueDate());
    }

    @ParameterizedTest
    @EnumSource(
        value = TicketStatus.class,
        names = {"RESOLVED", "CLOSED"}
    )
    void shouldNotMarkCompletedTicketsAsOverdue(
        TicketStatus status) {

        Ticket ticket = new Ticket(
            "Completed ticket",
            "Description",
            TicketPriority.HIGH
        );

        ticket.setDueDate(LocalDate.now().minusDays(5));
        ticket.setStatus(status);

        TicketResponse response = TicketResponse.from(ticket);

        assertFalse(response.overdue());
    }

    @ParameterizedTest
    @EnumSource(
        value = TicketStatus.class,
        names = {"OPEN", "IN_PROGRESS"}
    )
    void shouldMarkUnfinishedTicketsAsOverdue(
        TicketStatus status) {

        Ticket ticket = new Ticket(
            "Pending ticket",
            "Description",
            TicketPriority.MEDIUM
        );

        ticket.setDueDate(LocalDate.now().minusDays(2));
        ticket.setStatus(status);

        assertTrue(TicketResponse.from(ticket).overdue());
    }

    @Test
    void shouldNotMarkTicketDueTodayAsOverdue() {
        Ticket ticket = new Ticket(
            "Ticket due today",
            "Description",
            TicketPriority.HIGH
        );

        ticket.setDueDate(LocalDate.now());

        assertFalse(TicketResponse.from(ticket).overdue());
    }

    @Test
    void shouldNotMarkFutureTicketAsOverdue() {
        Ticket ticket = new Ticket(
            "Future ticket",
            "Description",
            TicketPriority.LOW
        );

        ticket.setDueDate(LocalDate.now().plusDays(5));

        assertFalse(TicketResponse.from(ticket).overdue());
    }

    @Test
    void shouldAcceptTicketWithoutDueDate() {
        Ticket ticket = new Ticket(
            "Ticket without deadline",
            "Description",
            TicketPriority.LOW
        );

        TicketResponse response = TicketResponse.from(ticket);

        assertNull(response.dueDate());
        assertFalse(response.overdue());
    }

}
