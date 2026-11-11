package com.ibm.tickets.dto;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketActivity;
import com.ibm.tickets.model.TicketComment;
import com.ibm.tickets.model.TicketPriority;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies the complete timeline response contract.
 * @author Gabriel Guimaraes
 */
class TicketActivityResponseTest {
    private final Ticket ticket = new Ticket("Title", "Description", TicketPriority.HIGH);

    @Test
    void mapsEveryActivityFieldIncludingNullPreviousValue() {
        TicketActivity activity = new TicketActivity(
                ticket, "Gabriel", "ASSIGNEE_CHANGED", null, "Owner");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-11T10:00:00-03:00");
        ReflectionTestUtils.setField(activity, "createdAt", createdAt);

        assertEquals(new TicketActivityResponse(
                "history", "Gabriel", "ASSIGNEE_CHANGED", null, "Owner", null, createdAt),
                TicketActivityResponse.fromActivity(activity));
    }

    @Test
    void preservesPreviousValueAndNullableNewValueAndTimestamp() {
        TicketActivity activity = new TicketActivity(
                ticket, "Gabriel", "ASSIGNEE_CHANGED", "Previous owner", null);
        ReflectionTestUtils.setField(activity, "createdAt", null);

        assertEquals(new TicketActivityResponse(
                "history", "Gabriel", "ASSIGNEE_CHANGED", "Previous owner", null, null, null),
                TicketActivityResponse.fromActivity(activity));
    }

    @Test
    void mapsEveryCommentFieldAndPreservesText() {
        TicketComment comment = new TicketComment(ticket, "Gabriel", "<b>Literal text</b>");
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-11-11T11:00:00Z");
        ReflectionTestUtils.setField(comment, "createdAt", createdAt);

        assertEquals(new TicketActivityResponse(
                "comment", "Gabriel", "COMMENT", null, null, "<b>Literal text</b>", createdAt),
                TicketActivityResponse.fromComment(comment));
    }
}
