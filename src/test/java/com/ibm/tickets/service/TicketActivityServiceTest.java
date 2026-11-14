
package com.ibm.tickets.service;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ibm.tickets.dto.TicketActivityResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketActivity;
import com.ibm.tickets.model.TicketComment;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.repository.TicketActivityRepository;
import com.ibm.tickets.repository.TicketCommentRepository;
import com.ibm.tickets.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
class TicketActivityServiceTest {

    @Mock
    private TicketActivityRepository activityRepository;

    @Mock
    private TicketCommentRepository commentRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketActivityService activityService;

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = new Ticket(
                "Login issue",
                "Cannot sign in",
                TicketPriority.HIGH
        );
    }

    @Test
    void shouldRecordActivityWhenValueChanges() {
        activityService.record(
                ticket,
                "admin",
                "STATUS_CHANGED",
                "OPEN",
                "IN_PROGRESS"
        );

        ArgumentCaptor<TicketActivity> captor =
                ArgumentCaptor.forClass(TicketActivity.class);

        verify(activityRepository).save(captor.capture());

        TicketActivity activity = captor.getValue();

        assertAll(
                () -> assertSame(ticket, activity.getTicket()),
                () -> assertEquals("admin", activity.getAuthor()),
                () -> assertEquals(
                        "STATUS_CHANGED",
                        activity.getActivityType()
                ),
                () -> assertEquals("OPEN", activity.getOldValue()),
                () -> assertEquals(
                        "IN_PROGRESS",
                        activity.getNewValue()
                )
        );
    }

    @Test
    void shouldNotRecordActivityWhenValueDoesNotChange() {
        activityService.record(
                ticket,
                "admin",
                "PRIORITY_CHANGED",
                "HIGH",
                "HIGH"
        );

        verifyNoInteractions(activityRepository);
    }

    @Test
    void shouldRecordAssignmentFromNull() {
        activityService.record(
                ticket,
                "admin",
                "ASSIGNEE_CHANGED",
                null,
                "Gabriel"
        );

        ArgumentCaptor<TicketActivity> captor =
                ArgumentCaptor.forClass(TicketActivity.class);

        verify(activityRepository).save(captor.capture());

        TicketActivity activity = captor.getValue();

        assertNull(activity.getOldValue());
        assertEquals("Gabriel", activity.getNewValue());
    }

    @Test
    void shouldMergeActivitiesAndCommentsChronologically() {
        OffsetDateTime firstTime =
                OffsetDateTime.parse("2026-10-01T10:00:00Z");

        OffsetDateTime commentTime =
                OffsetDateTime.parse("2026-10-01T11:00:00Z");

        OffsetDateTime lastTime =
                OffsetDateTime.parse("2026-10-01T12:00:00Z");

        TicketActivity first = new TicketActivity(
                ticket,
                "admin",
                "STATUS_CHANGED",
                "OPEN",
                "IN_PROGRESS"
        );

        TicketActivity last = new TicketActivity(
                ticket,
                "Rodrigo",
                "PRIORITY_CHANGED",
                "HIGH",
                "URGENT"
        );

        TicketComment comment = new TicketComment(
                ticket,
                "Gabriel",
                "Investigating"
        );

        ReflectionTestUtils.setField(
                first, "createdAt", firstTime
        );

        ReflectionTestUtils.setField(
                last, "createdAt", lastTime
        );

        ReflectionTestUtils.setField(
                comment, "createdAt", commentTime
        );

        when(ticketRepository.existsById(1L))
                .thenReturn(true);

        when(activityRepository
                .findAllByTicketIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(first, last));

        when(commentRepository
                .findAllByTicketIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(comment));

        List<TicketActivityResponse> timeline =
                activityService.findByTicketId(1L);

        assertEquals(3, timeline.size());

        assertAll(
                () -> assertEquals(
                        List.of(firstTime, commentTime, lastTime),
                        timeline.stream()
                                .map(TicketActivityResponse::createdAt)
                                .toList()
                ),
                () -> assertEquals(
                        "history", timeline.get(0).type()
                ),
                () -> assertEquals(
                        "STATUS_CHANGED",
                        timeline.get(0).activityType()
                ),
                () -> assertEquals(
                        "admin", timeline.get(0).author()
                ),
                () -> assertEquals(
                        "comment", timeline.get(1).type()
                ),
                () -> assertEquals(
                        "Investigating", timeline.get(1).content()
                ),
                () -> assertEquals(
                        "Gabriel", timeline.get(1).author()
                ),
                () -> assertEquals(
                        "PRIORITY_CHANGED",
                        timeline.get(2).activityType()
                ),
                () -> assertEquals(
                        "URGENT", timeline.get(2).newValue()
                )
        );
    }

    @Test
    void shouldReturnEmptyTimelineWhenTicketHasNoEvents() {
        when(ticketRepository.existsById(1L))
                .thenReturn(true);

        List<TicketActivityResponse> timeline =
                activityService.findByTicketId(1L);

        assertTrue(timeline.isEmpty());

        verify(activityRepository)
                .findAllByTicketIdOrderByCreatedAtAsc(1L);

        verify(commentRepository)
                .findAllByTicketIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void shouldRejectNonexistentTicketWithoutQueryingHistory() {
        when(ticketRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                TicketNotFoundException.class,
                () -> activityService.findByTicketId(99L)
        );

        verifyNoInteractions(
                activityRepository,
                commentRepository
        );
    }
	@Test
	void shouldRecordDueDateChange() {
		activityService.record(
			ticket,
			"Mariana",
			"DUE_DATE_CHANGED",
			"2026-11-20",
			"2026-11-25"
		);

		ArgumentCaptor<TicketActivity> captor = ArgumentCaptor.forClass(TicketActivity.class);

		verify(activityRepository).save(captor.capture());

		TicketActivity activity = captor.getValue();

		assertEquals("DUE_DATE_CHANGED", activity.getActivityType());
		assertEquals("Mariana", activity.getAuthor());
		assertEquals("2026-11-20", activity.getOldValue());
		assertEquals("2026-11-25", activity.getNewValue());
	}

	@Test
	void shouldNotRecordUnchangedDueDate() {
		activityService.record(
			ticket,
			"Mariana",
			"DUE_DATE_CHANGED",
			"2026-11-20",
			"2026-11-20"
		);

		verifyNoInteractions(activityRepository);
	}
}
