package com.ibm.tickets.repository;

import com.ibm.tickets.model.Project;
import jakarta.persistence.EntityManager;
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
        Project first = repository.saveAndFlush(new Project("Ticket System", null));
        Project second = repository.saveAndFlush(new Project("C14 Ticket System", null));
        Project sameName = repository.saveAndFlush(new Project("Ticket System", null));
        entityManager.clear();

        assertEquals(3, repository.count());
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
}
