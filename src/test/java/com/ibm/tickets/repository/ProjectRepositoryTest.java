package com.ibm.tickets.repository;

import com.ibm.tickets.model.Project;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests project persistence through JPA and the migrated schema.
 * @author Gabriel Guimaraes
 */
@DataJpaTest
class ProjectRepositoryTest {
    @Autowired
    private ProjectRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savesAndReloadsProjectWithGeneratedIdAndFullDescription() {
        String description = "Project description ".repeat(100);
        Project saved = repository.saveAndFlush(new Project(" Ticket System ", description));
        assertNotNull(saved.getId());
        entityManager.clear();

        Project reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals(" Ticket System ", reloaded.getName());
        assertEquals(description, reloaded.getDescription());
    }

    @Test
    void preservesDifferentNamesAndIndependentIdentities() {
        long existingProjects = repository.count();
        Project first = repository.saveAndFlush(new Project("Ticket System", null));
        Project second = repository.saveAndFlush(new Project("C14 Ticket System", null));
        Project sameName = repository.saveAndFlush(new Project("Ticket System", null));
        entityManager.clear();

        assertEquals(existingProjects + 3, repository.count());
        assertNotEquals(first.getId(), second.getId());
        assertNotEquals(first.getId(), sameName.getId());
        assertEquals("Ticket System", repository.findById(first.getId()).orElseThrow().getName());
        assertEquals("C14 Ticket System", repository.findById(second.getId()).orElseThrow().getName());
        assertNull(repository.findById(sameName.getId()).orElseThrow().getDescription());
    }

    @Test
    void returnsEmptyForUnknownProject() {
        assertTrue(repository.findById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void countsTicketsUsingExactLegacyProjectName() {
        repository.saveAndFlush(new Project("Catalog Exact", null));
        repository.saveAndFlush(new Project("catalog exact", null));
        repository.saveAndFlush(new Project(" Catalog Exact ", null));
        insertTicket("First exact", "Catalog Exact");
        insertTicket("Second exact", "Catalog Exact");
        insertTicket("Lowercase", "catalog exact");
        insertTicket("Unmatched", "Missing project");
        entityManager.clear();

        List<ProjectCatalogView> catalog = repository.findCatalog();

        assertEquals(2, ticketCount(catalog, "Catalog Exact"));
        assertEquals(1, ticketCount(catalog, "catalog exact"));
        assertEquals(0, ticketCount(catalog, " Catalog Exact "));
    }

    @Test
    void listsEveryPersistedProjectIncludingDuplicateNames() {
        Project first = repository.saveAndFlush(new Project("Catalog Duplicate", "First"));
        Project second = repository.saveAndFlush(new Project("Catalog Duplicate", "Second"));
        insertTicket("Legacy duplicate", "Catalog Duplicate");
        entityManager.createNativeQuery("""
                insert into tickets (title, description, project, assignee, project_id)
                values ('Linked duplicate', 'Description', 'Catalog Duplicate', 'Owner', :projectId),
                       ('Linked stale name', 'Description', 'Different legacy name', 'Owner', :projectId)
                """)
                .setParameter("projectId", second.getId())
                .executeUpdate();
        entityManager.clear();

        List<ProjectCatalogView> duplicates = repository.findCatalog().stream()
                .filter(project -> project.getName().equals("Catalog Duplicate"))
                .toList();

        assertEquals(2, duplicates.size());
        assertEquals(List.of(first.getId(), second.getId()),
                duplicates.stream().map(ProjectCatalogView::getId).toList());
        assertEquals(List.of(1L, 2L),
                duplicates.stream().map(ProjectCatalogView::getTicketCount).toList());
    }

    private void insertTicket(String title, String project) {
        entityManager.createNativeQuery("""
                insert into tickets (title, description, project, assignee)
                values (:title, 'Description', :project, 'Owner')
                """)
                .setParameter("title", title)
                .setParameter("project", project)
                .executeUpdate();
    }

    private long ticketCount(List<ProjectCatalogView> catalog, String projectName) {
        return catalog.stream()
                .filter(project -> project.getName().equals(projectName))
                .findFirst()
                .orElseThrow()
                .getTicketCount();
    }
}
