package com.ibm.tickets.repository;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;

import jakarta.persistence.EntityManager;

/**
 * Integration tests for TicketRepository.
 *
 * @author Gabriel Guimaraes
 */
@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldListTicketsByCreationDateDescending() {
        Ticket firstTicket = createTicket(
                "First ticket",
                TicketStatus.OPEN);

        ticketRepository.saveAndFlush(firstTicket);

        Ticket secondTicket = createTicket(
                "Second ticket",
                TicketStatus.IN_PROGRESS);

        ticketRepository.saveAndFlush(secondTicket);

        // Explicit timestamps make this ordering test independent of clock resolution.
        entityManager.createNativeQuery("update tickets set created_at = :time where id = :id")
                .setParameter("time", OffsetDateTime.parse("2026-01-01T10:00:00Z"))
                .setParameter("id", firstTicket.getId()).executeUpdate();
        entityManager.createNativeQuery("update tickets set created_at = :time where id = :id")
                .setParameter("time", OffsetDateTime.parse("2026-01-01T11:00:00Z"))
                .setParameter("id", secondTicket.getId()).executeUpdate();
        entityManager.clear();

        List<Ticket> tickets = ticketRepository
                .findAllByOrderByCreatedAtDesc();

        assertAll(
                () -> assertEquals(2, tickets.size()),
                () -> assertEquals(
                        "Second ticket",
                        tickets.get(0).getTitle()),
                () -> assertEquals(
                        "First ticket",
                        tickets.get(1).getTitle()));
    }

    @Test
    void shouldFilterTicketsByStatus() {
        Ticket openTicket = createTicket(
                "Open ticket",
                TicketStatus.OPEN);

        Ticket closedTicket = createTicket(
                "Closed ticket",
                TicketStatus.CLOSED);

        ticketRepository.saveAndFlush(openTicket);
        ticketRepository.saveAndFlush(closedTicket);

        List<Ticket> tickets = ticketRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        TicketStatus.CLOSED);

        assertAll(
                () -> assertEquals(1, tickets.size()),
                () -> assertEquals(
                        "Closed ticket",
                        tickets.get(0).getTitle()),
                () -> assertEquals(
                        TicketStatus.CLOSED,
                        tickets.get(0).getStatus()));
    }

    private Project createProject(String name) {
        return projectRepository.saveAndFlush(
            new Project(name, "Repository integration test"));
}

    @Test
    void shouldListTicketsOnlyFromSelectedProject() {
        Project projectA = createProject("Project A");
        Project projectB = createProject("Project B");
        
        Ticket ticketA = createTicket("Ticket A", TicketStatus.OPEN);
        ticketA.setProjectReference(projectA);
        
        Ticket ticketB = createTicket("Ticket B", TicketStatus.OPEN);
        ticketB.setProjectReference(projectB);
        
        ticketRepository.saveAndFlush(ticketA);
        ticketRepository.saveAndFlush(ticketB);
        
        List<Ticket> tickets = ticketRepository
            .findAllByProjectReference_IdOrderByCreatedAtDesc(projectA.getId());
            
        assertAll(
            () -> assertEquals(1, tickets.size()),
            () -> assertEquals("Ticket A", tickets.get(0).getTitle()),
            () -> assertEquals(projectA.getId(), tickets.get(0).getProjectReference().getId()),
            () -> assertTrue(tickets.stream().noneMatch(
                    ticket -> ticket.getProjectReference().getId().equals(projectB.getId())))
        );
    }

    private Ticket createTicket(
            String title,
            TicketStatus status) {
        Ticket ticket = new Ticket(
                title,
                "Repository integration test",
                TicketPriority.HIGH);

        ticket.setProject("Ticket System");
        ticket.setAssignee("Gabriel");
        ticket.setStatus(status);

        return ticket;
    }

	@Test
	void shouldFilterTicketsByProjectAndPriority() {
		Project project = createProject("Project A");

		Ticket highTicket = createTicket("High ticket", TicketStatus.OPEN);
		highTicket.setProjectReference(project);

		Ticket lowTicket = createTicket("Low ticket", TicketStatus.OPEN);
		lowTicket.setProjectReference(project);
		lowTicket.setPriority(TicketPriority.LOW);

		ticketRepository.saveAndFlush(highTicket);
		ticketRepository.saveAndFlush(lowTicket);

		List<Ticket> tickets = ticketRepository
				.findAllByProjectReference_IdAndPriorityOrderByCreatedAtDesc(project.getId(), TicketPriority.HIGH);

		assertEquals(1, tickets.size());
		assertEquals("High ticket", tickets.get(0).getTitle());
	}

	@Test
	void shouldFilterTicketsByProjectAndStatus() {
		Project project = createProject("Project A");

		Ticket openTicket = createTicket("Open ticket", TicketStatus.OPEN);
		openTicket.setProjectReference(project);

		Ticket closedTicket = createTicket("Closed ticket", TicketStatus.CLOSED);
		closedTicket.setProjectReference(project);

		ticketRepository.saveAndFlush(openTicket);
		ticketRepository.saveAndFlush(closedTicket);

		List<Ticket> tickets = ticketRepository
				.findAllByProjectReference_IdAndStatusOrderByCreatedAtDesc(project.getId(), TicketStatus.CLOSED);

		assertEquals(1, tickets.size());
		assertEquals("Closed ticket", tickets.get(0).getTitle());
	}

	@Test
	void shouldFilterTicketsByProjectStatusAndPriority() {
		Project project = createProject("Project A");

		Ticket matchingTicket = createTicket("Matching ticket", TicketStatus.OPEN);
		matchingTicket.setProjectReference(project);

		Ticket wrongStatus = createTicket("Wrong status", TicketStatus.CLOSED);
		wrongStatus.setProjectReference(project);

		Ticket wrongPriority = createTicket("Wrong priority", TicketStatus.OPEN);
		wrongPriority.setProjectReference(project);
		wrongPriority.setPriority(TicketPriority.LOW);

		ticketRepository.saveAndFlush(matchingTicket);
		ticketRepository.saveAndFlush(wrongStatus);
		ticketRepository.saveAndFlush(wrongPriority);

		List<Ticket> tickets = ticketRepository
				.findAllByProjectReference_IdAndStatusAndPriorityOrderByCreatedAtDesc(
						project.getId(), TicketStatus.OPEN, TicketPriority.HIGH);

		assertEquals(1, tickets.size());
		assertEquals("Matching ticket", tickets.get(0).getTitle());
	}

}