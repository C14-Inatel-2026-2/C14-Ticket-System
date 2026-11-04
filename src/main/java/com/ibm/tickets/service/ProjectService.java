package com.ibm.tickets.service;

import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.repository.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Provides read-only access to the persisted project catalog.
 * @author Gabriel Guimaraes
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectResponse> getProjects() {
        return projectRepository.findCatalog().stream()
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        Math.toIntExact(project.getTicketCount())))
                .toList();
    }
}
