package com.ibm.tickets.service;

import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.repository.ProjectCatalogView;
import com.ibm.tickets.repository.ProjectRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests project catalog mapping independently of the web layer.
 * @author Gabriel Guimaraes
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectRepository repository;

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
