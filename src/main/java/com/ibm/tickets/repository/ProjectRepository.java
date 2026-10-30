package com.ibm.tickets.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ibm.tickets.model.Project;

/**
 * Provides persistence operations for projects.
 * @author Gabriel Guimaraes
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
