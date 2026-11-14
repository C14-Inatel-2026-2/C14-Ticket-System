package com.ibm.tickets.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.ProjectRequest;
import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.repository.ProjectCatalogView;
import com.ibm.tickets.repository.ProjectRepository;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.UserRepository;

/**
 * Tests project catalog mapping independently of the web layer.
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository repository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private ProjectService service;

    @Test
    void mapsCatalogRowsToProjectResponses() {
        when(repository.findCatalog()).thenReturn(List.of(
                catalogRow(10L, "First", "First description", 3),
                catalogRow(20L, "Second", null, 0)));

        List<ProjectResponse> projects = service.getProjects();

        assertEquals(List.of(
                new ProjectResponse(10L, "First", "First description", 3),
                new ProjectResponse(20L, "Second", null, 0)), projects);
    }

    @Test
    void returnsEmptyListWhenRepositoryHasNoProjects() {
        when(repository.findCatalog()).thenReturn(List.of());

        assertEquals(List.of(), service.getProjects());
    }

    @Test
    void rejectsTicketCountThatDoesNotFitProjectResponse() {
        when(repository.findCatalog()).thenReturn(List.of(
                catalogRow(10L, "Overflow", null, (long) Integer.MAX_VALUE + 1)));

        assertThrows(ArithmeticException.class, service::getProjects);
    }

    @Test
    void throwsNotFoundWhenUpdatingNonExistingProject() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.update(99L, new ProjectRequest(
                        "Project", "Description", List.of()))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void throwsNotFoundWhenDeletingNonExistingProject() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.delete(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void throwsNotFoundWhenGettingMembersOfNonExistingProject() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getMemberIds(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
}

    private ProjectCatalogView catalogRow(
            Long id,
            String name,
            String description,
            long ticketCount) {
        return new ProjectCatalogView() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public long getTicketCount() {
                return ticketCount;
            }
        };
    }
}
