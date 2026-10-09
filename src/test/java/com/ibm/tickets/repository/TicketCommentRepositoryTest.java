package com.ibm.tickets.repository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketComment;
import com.ibm.tickets.model.TicketPriority;

/**
 * Integration tests for TicketCommentRepository.
 *
 * @author Gabriel Guimaraes
 */
@DataJpaTest
class TicketCommentRepositoryTest {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketCommentRepository commentRepository;

    @Test
    void shouldFindCommentsOrderedByCreatedAtDescending() {
        Ticket ticket = createAndSaveTicket();

        TicketComment firstComment = new TicketComment(
                ticket,
                "Gabriel",
                "The problem is being investigated.");

        commentRepository.saveAndFlush(firstComment);

        TicketComment secondComment = new TicketComment(
                ticket,
                "Rodrigo",
                "The problem has been resolved.");

        commentRepository.saveAndFlush(secondComment);

        List<TicketComment> comments =
                commentRepository
                        .findAllByTicketIdOrderByCreatedAtDesc(
                                ticket.getId());

        assertAll(
                () -> assertEquals(2, comments.size()),
                () -> assertEquals(
                        "The problem has been resolved.",
                        comments.get(0).getContent()),
                () -> assertEquals(
                        "The problem is being investigated.",
                        comments.get(1).getContent()),
                () -> assertEquals(
                        ticket.getId(),
                        comments.get(0).getTicket().getId()));
    }

    @Test
    void shouldReturnEmptyListWhenTicketHasNoComments() {
        Ticket ticket = createAndSaveTicket();

        List<TicketComment> comments =
                commentRepository
                        .findAllByTicketIdOrderByCreatedAtDesc(
                                ticket.getId());

        assertTrue(comments.isEmpty());
    }

    private Ticket createAndSaveTicket() {
        Ticket ticket = new Ticket(
                "Printer problem",
                "The printer is not working",
                TicketPriority.HIGH);

        ticket.setProject("Internal Systems");
        ticket.setAssignee("Gabriel");

        return ticketRepository.saveAndFlush(ticket);
    }
}