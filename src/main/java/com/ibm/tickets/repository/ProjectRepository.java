package com.ibm.tickets.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.ibm.tickets.model.Project;

/**
 * Provides persistence operations for projects.
 * @author Gabriel Guimaraes
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query(value = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.description AS description,
                   COUNT(t.id) AS "ticketCount"
            FROM projects p
            LEFT JOIN tickets t ON t.project_id = p.id
                OR (t.project_id IS NULL AND t.project = p.name
                    AND p.id = (SELECT MIN(legacy.id) FROM projects legacy WHERE legacy.name = p.name))
            GROUP BY p.id, p.name, p.description
            ORDER BY p.id
            """, nativeQuery = true)
    List<ProjectCatalogView> findCatalog();

    List<Project> findDistinctByMembers_IdOrderByIdAsc(Long userId);
    boolean existsByIdAndMembers_Id(Long projectId, Long userId);
    
}
