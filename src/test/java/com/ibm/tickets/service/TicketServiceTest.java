package com.ibm.tickets.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.exception.TicketNotInProjectException;
import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.User;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.ProjectRepository;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.UserRepository;

/**
 * Unit tests for TicketService.
 *
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    @Mock
    private TicketActivityService activityService;

    private static final Long PROJECT_ID = 1L;
    private static final Long UPDATED_PROJECT_ID = 2L;

    private Project createProject(Long id, String name) {
        Project project = new Project(name, "Test description");

        org.springframework.test.util.ReflectionTestUtils.setField(project, "id", id);

        return project;
    }

	private User createUser(Long id, String name) {
		User user = new User(
				name,
				name.toLowerCase() + "@example.com",
				UserRole.DEVELOPER);

		org.springframework.test.util.ReflectionTestUtils
				.setField(user, "id", id);

		return user;
	}

    @Test
    void shouldCreateTicket() {
        TicketRequest request = new TicketRequest(
                "Printer problem",
                "The printer is not working",
                PROJECT_ID,
                "Gabriel",
				1L,
                TicketPriority.HIGH);

        Project project = createProject(PROJECT_ID, "Internal Systems");
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
		when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "Gabriel")));

        when(ticketRepository.saveAndFlush(any(Ticket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.create(request);
        verify(projectRepository).findById(PROJECT_ID);

        assertAll(
                () -> assertEquals(
                        "Printer problem",
                        response.title()),
                () -> assertEquals(
                        "The printer is not working",
                        response.description()),
                () -> assertEquals(
                        "Internal Systems",
                        response.project()),
                () -> assertEquals(
                        "Gabriel",
                        response.assignee()),
				() -> assertEquals(
					1L,
					response.assigneeUserId()),
                () -> assertEquals(
                        TicketPriority.HIGH,
                        response.priority()));

        verify(ticketRepository).saveAndFlush(
            org.mockito.ArgumentMatchers.argThat(ticket ->
                ticket.getProjectReference() != null
                && ticket.getProjectReference().getId().equals(PROJECT_ID)
            )
        );
    }

    @Test
    void shouldListAllTickets() {
        Ticket ticket = createTicket(
                "Printer problem",
                TicketStatus.OPEN);

        when(ticketRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(ticket));

        List<TicketResponse> responses = ticketService.findAll(null);

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(
                        "Printer problem",
                        responses.get(0).title()),
                () -> assertEquals(
                        "Internal Systems",
                        responses.get(0).project()),
                () -> assertEquals(
                        TicketStatus.OPEN,
                        responses.get(0).status()));

        verify(ticketRepository)
                .findAllByOrderByCreatedAtDesc();
    }

    @Test
    void shouldFilterTicketsByStatus() {
        Ticket ticket = createTicket(
                "Database problem",
                TicketStatus.IN_PROGRESS);

        when(ticketRepository
                .findAllByStatusOrderByCreatedAtDesc(
                        TicketStatus.IN_PROGRESS))
                .thenReturn(List.of(ticket));

        List<TicketResponse> responses = ticketService.findAll(
                TicketStatus.IN_PROGRESS);

        assertAll(
                () -> assertEquals(1, responses.size()),
                () -> assertEquals(
                        "Database problem",
                        responses.get(0).title()),
                () -> assertEquals(
                        TicketStatus.IN_PROGRESS,
                        responses.get(0).status()));

        verify(ticketRepository)
                .findAllByStatusOrderByCreatedAtDesc(
                        TicketStatus.IN_PROGRESS);
    }

    @Test
    void shouldThrowExceptionWhenTicketDoesNotExist() {
        when(ticketRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.findById(99L));

        verify(ticketRepository).findById(99L);
    }

    @Test
    void shouldFindTicketWhenItBelongsToProject() {
        Ticket ticket = createTicket("Printer problem", TicketStatus.OPEN);

        org.springframework.test.util.ReflectionTestUtils.setField(ticket, "id", 1L);

        ticket.setProjectReference(createProject(PROJECT_ID, "Internal Systems"));

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        TicketResponse response = ticketService.findByIdInProject(1L, PROJECT_ID);

        assertEquals(1L, response.id());
        assertEquals(PROJECT_ID, response.projectId());

        verify(ticketRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTicketBelongsToAnotherProject() {
        Ticket ticket = createTicket("Printer problem", TicketStatus.OPEN);
        ticket.setProjectReference(createProject(UPDATED_PROJECT_ID, "Other project"));

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(
                TicketNotInProjectException.class,
                () -> ticketService.findByIdInProject(1L, PROJECT_ID));

        verify(ticketRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTicketHasNoProject() {
        Ticket ticket = createTicket("Printer problem", TicketStatus.OPEN);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        assertThrows(
                TicketNotInProjectException.class,
                () -> ticketService.findByIdInProject(1L, PROJECT_ID));

        verify(ticketRepository).findById(1L);
    }

    @Test
    void shouldThrowTicketNotFoundWhenTicketDoesNotExistInProject() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                TicketNotFoundException.class,
                () -> ticketService.findByIdInProject(99L, PROJECT_ID));

        verify(ticketRepository).findById(99L);
    }


    @Test
    void shouldUpdateTicket() {
        Ticket ticket = createTicket(
                "Old title",
                TicketStatus.OPEN);

        TicketRequest request = new TicketRequest(
                "Updated title",
                "Updated description",
                UPDATED_PROJECT_ID,
                "Mariana",
				2L,
                TicketPriority.MEDIUM);

        Project updatedProject = createProject(UPDATED_PROJECT_ID, "Updated project");

        when(projectRepository.findById(UPDATED_PROJECT_ID)).thenReturn(Optional.of(updatedProject));
		when(userRepository.findById(2L)).thenReturn(Optional.of(createUser(2L, "Mariana")));

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        TicketResponse response = ticketService.update(
                1L,
                request, "Admin");

        assertAll(
                () -> assertEquals(
                        "Updated title",
                        response.title()),
                () -> assertEquals(
                        "Updated description",
                        response.description()),
                () -> assertEquals(
                        "Updated project",
                        response.project()),
                () -> assertEquals(
                        "Mariana",
                        response.assignee()),
				() -> assertEquals(
						2L,
						response.assigneeUserId()),
                () -> assertEquals(
                        TicketPriority.MEDIUM,
                        response.priority()));

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).saveAndFlush(ticket);

        verify(activityService).record(
                ticket,
                "Admin",
                "ASSIGNEE_CHANGED",
                "Gabriel",
                "Mariana"
        );

        verify(activityService).record(
                ticket,
                "Admin",
                "PRIORITY_CHANGED",
                "HIGH",
                "MEDIUM"
        );
    }

    @Test
    void shouldUpdateTicketStatus() {
        Ticket ticket = createTicket(
                "Printer problem",
                TicketStatus.OPEN);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        when(ticketRepository.saveAndFlush(ticket))
                .thenReturn(ticket);

        TicketResponse response = ticketService.updateStatus(
                1L,
                TicketStatus.CLOSED, "admin");

        assertEquals(
                TicketStatus.CLOSED,
                response.status());

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).saveAndFlush(ticket);
        verify(activityService).record(
            ticket,
            "admin",
            "STATUS_CHANGED",
            "OPEN",
            "CLOSED"
        );
    }

    @Test
    void shouldDeleteTicket() {
        Ticket ticket = createTicket(
                "Printer problem",
                TicketStatus.OPEN);

        when(ticketRepository.findById(1L))
                .thenReturn(Optional.of(ticket));

        ticketService.delete(1L);

        verify(ticketRepository).findById(1L);
        verify(ticketRepository).delete(ticket);
    }

    @Test
    void shouldFilterTicketsByPriorityAndPreserveRepositoryOrder() {
        Ticket first = createTicket("First", TicketStatus.OPEN);
        Ticket second = createTicket("Second", TicketStatus.CLOSED);
        when(ticketRepository.findAllByPriorityOrderByCreatedAtDesc(TicketPriority.HIGH))
                .thenReturn(List.of(first, second));

        assertEquals(List.of(TicketResponse.from(first), TicketResponse.from(second)),
                ticketService.findAll(null, TicketPriority.HIGH));
        verify(ticketRepository).findAllByPriorityOrderByCreatedAtDesc(TicketPriority.HIGH);
        org.mockito.Mockito.verifyNoMoreInteractions(ticketRepository);
    }

    @Test
    void shouldCombineStatusAndPriorityFilters() {
        Ticket ticket = createTicket("Combined", TicketStatus.IN_PROGRESS);
        when(ticketRepository.findAllByStatusAndPriorityOrderByCreatedAtDesc(
                TicketStatus.IN_PROGRESS, TicketPriority.HIGH)).thenReturn(List.of(ticket));

        assertEquals(List.of(TicketResponse.from(ticket)),
                ticketService.findAll(TicketStatus.IN_PROGRESS, TicketPriority.HIGH));
        verify(ticketRepository).findAllByStatusAndPriorityOrderByCreatedAtDesc(
                TicketStatus.IN_PROGRESS, TicketPriority.HIGH);
        org.mockito.Mockito.verifyNoMoreInteractions(ticketRepository);
    }

    private Ticket createTicket(
            String title,
            TicketStatus status) {
        Ticket ticket = new Ticket(
                title,
                "Ticket description",
                TicketPriority.HIGH);

        ticket.setProject("Internal Systems");
        ticket.setAssignee("Gabriel");
        ticket.setStatus(status);

        return ticket;
    }


	@Test
	void shouldNotRecordActivityWhenStatusDoesNotChange() {
			Ticket ticket = createTicket(
					"Printer problem",
					TicketStatus.OPEN);

			when(ticketRepository.findById(1L))
					.thenReturn(Optional.of(ticket));

			TicketResponse response = ticketService.updateStatus(
					1L,
					TicketStatus.OPEN,
					"admin"
			);

			assertEquals(TicketStatus.OPEN, response.status());

			verify(ticketRepository, never())
					.saveAndFlush(any(Ticket.class));

			verifyNoInteractions(activityService);
	}
	@Test
	void shouldRejectNonexistentAssigneeUser() {
		TicketRequest request = new TicketRequest(
				"Test ticket",
				"Test description",
				PROJECT_ID,
				"Unknown user",
				999L,
				TicketPriority.HIGH);

		Project project = createProject(PROJECT_ID, "Internal Systems");

		when(projectRepository.findById(PROJECT_ID))
				.thenReturn(Optional.of(project));

		when(userRepository.findById(999L))
				.thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> ticketService.create(request));

		assertEquals(400, exception.getStatusCode().value());

		verify(ticketRepository, never())
				.saveAndFlush(any(Ticket.class));
	}

@Test
void shouldCreateTicketWithDueDate() {
    LocalDate dueDate = LocalDate.now().plusDays(5);

    TicketRequest request = new TicketRequest(
            "Ticket with deadline",
            "Description",
            PROJECT_ID,
            "Gabriel",
            1L,
            TicketPriority.HIGH,
            dueDate
    );

    when(projectRepository.findById(PROJECT_ID))
            .thenReturn(Optional.of(
                    createProject(PROJECT_ID, "Internal Systems")
            ));

    when(userRepository.findById(1L))
            .thenReturn(Optional.of(createUser(1L, "Gabriel")));

    when(ticketRepository.saveAndFlush(any(Ticket.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    TicketResponse response = ticketService.create(request);

    assertEquals(dueDate, response.dueDate());
    assertFalse(response.overdue());

    verify(ticketRepository).saveAndFlush(
            org.mockito.ArgumentMatchers.argThat(
                    ticket -> dueDate.equals(ticket.getDueDate())
            )
    );
}

@Test
void shouldUpdateDueDateAndRecordHistory() {
    Ticket ticket = createTicket(
            "Ticket to update",
            TicketStatus.OPEN
    );

    ticket.setDueDate(LocalDate.of(2026, 11, 20));

    LocalDate newDueDate = LocalDate.of(2026, 11, 25);

    TicketRequest request = new TicketRequest(
            "Ticket to update",
            "Description",
            PROJECT_ID,
            "Gabriel",
            1L,
            TicketPriority.HIGH,
            newDueDate
    );

    when(ticketRepository.findById(1L))
            .thenReturn(Optional.of(ticket));

    when(projectRepository.findById(PROJECT_ID))
            .thenReturn(Optional.of(
                    createProject(PROJECT_ID, "Internal Systems")
            ));

    when(userRepository.findById(1L))
            .thenReturn(Optional.of(createUser(1L, "Gabriel")));

    when(ticketRepository.saveAndFlush(ticket))
            .thenReturn(ticket);

    TicketResponse response =
            ticketService.update(1L, request, "Mariana");

    assertEquals(newDueDate, response.dueDate());

    verify(activityService).record(
            ticket,
            "Mariana",
            "DUE_DATE_CHANGED",
            "2026-11-20",
            "2026-11-25"
    );
}

@Test
void shouldRemoveDueDateAndRecordHistory() {
    Ticket ticket = createTicket("Ticket to update", TicketStatus.OPEN);

    ticket.setDueDate(LocalDate.of(2026, 11, 20));

    TicketRequest request = new TicketRequest(
        "Ticket to update",
        "Description",
        PROJECT_ID,
        "Gabriel",
        1L,
        TicketPriority.HIGH,
        null
    );

    when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

    when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(createProject(PROJECT_ID, "Internal Systems")));

    when(userRepository.findById(1L)).thenReturn(Optional.of(createUser(1L, "Gabriel")));

    when(ticketRepository.saveAndFlush(ticket)).thenReturn(ticket);

    TicketResponse response = ticketService.update(1L, request, "Mariana");

    assertNull(response.dueDate());

    verify(activityService).record(
        ticket,
        "Mariana",
        "DUE_DATE_CHANGED",
        "2026-11-20",
        null
    );
}

}
