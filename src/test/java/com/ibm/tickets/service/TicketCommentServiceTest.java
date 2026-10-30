package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.TicketCommentRequest;
import com.ibm.tickets.dto.TicketCommentResponse;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketComment;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.repository.TicketCommentRepository;
import com.ibm.tickets.repository.TicketRepository;

public class TicketCommentServiceTest {
    
    private TicketCommentRepository commentRepository;
    private TicketRepository ticketRepository;
    private TicketCommentService commentService;

    private Ticket ticket;

    @BeforeEach
    void setUp(){
        commentRepository = mock(TicketCommentRepository.class);
        ticketRepository = mock(TicketRepository.class);

        commentService = new TicketCommentService(commentRepository,
            ticketRepository);
        
        ticket = new Ticket("Erro no login", 
        "Não consigo acessar o sistema", 
        TicketPriority.HIGH);
    }

    @Test 
    void shouldCreateComment(){
        when(ticketRepository.findById(1L))
            .thenReturn(Optional.of(ticket));

        when(commentRepository.save(any(TicketComment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        TicketCommentRequest request = new TicketCommentRequest("Comentário de teste");
        TicketCommentResponse response = commentService.create(1L, "Rodrigo", request);

        assertEquals("Rodrigo", response.author());
        assertEquals("Comentário de teste", response.content());
        verify(commentRepository).save(any(TicketComment.class));
    }

    @Test 
    void shouldAssociateCommentWithCorrectTicket(){
        when(ticketRepository.findById(1L))
            .thenReturn(Optional.of(ticket));
        
        when(commentRepository.save(any(TicketComment.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        commentService.create(1L, "Rodrigo", 
            new TicketCommentRequest("Novo comentário"));

        var captor = org.mockito.ArgumentCaptor
                .forClass(TicketComment.class);
        
        verify(commentRepository).save(captor.capture());

        TicketComment savedComment = captor.getValue();

        assertSame(ticket, savedComment.getTicket());
        assertEquals("Rodrigo",  savedComment.getAuthor());
        assertEquals("Novo comentário", savedComment.getContent());
    }

    @Test 
    void shouldListTicketsComments(){
        TicketComment first = new TicketComment(ticket, 
            "Rodrigo", "Primeiro");
        TicketComment second = new TicketComment(ticket, 
            "Gabriel", "Segundo");
        
        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAllByTicketIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of(second, first));

        List<TicketCommentResponse> comments = commentService.findByTicketId(1L);

        assertEquals(2, comments.size());
        assertEquals("Segundo", comments.get(0).content());
        assertEquals("Primeiro", comments.get(1).content());
    }

    @Test 
    void shouldReturnEmptyListWhenNoCommentsExist(){
        when(ticketRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findAllByTicketIdOrderByCreatedAtDesc(1L))
            .thenReturn(List.of());
        
        List<TicketCommentResponse> comments = commentService.findByTicketId(1L);
        assertTrue(comments.isEmpty());
    }

    @Test
    void shouldRejectCommentForNonexistentTicket(){
        when(ticketRepository.findById(999L))
            .thenReturn(Optional.empty());
        
        assertThrows(ResponseStatusException.class, 
            () -> commentService.create(999L, "Rodrigo", 
                new TicketCommentRequest("Teste")
            )
        );

        verify(commentRepository, never()).save(any());
    }
}
