package com.ibm.tickets.service;

import com.ibm.tickets.dto.ProjectRequest;
import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.User;
import com.ibm.tickets.repository.ProjectRepository;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Provides read-only access to the persisted project catalog.
 * @author Gabriel Guimaraes
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public ProjectService(ProjectRepository projectRepository, 
                UserRepository userRepository,
                TicketRepository ticketRepository
    ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
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

    public ProjectResponse getProject(Long id){
        return getProjects().stream()
                .filter(project -> project.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project not found"
                ));
    }

    @Transactional 
    public ProjectResponse create(ProjectRequest request){
        Set<User> membersIds = validateMembers(request.membersIds());

        Project project = new Project(request.name().trim(), request.description());
        project.setMembers(membersIds);
        project = projectRepository.saveAndFlush(project);

        return getProject(project.getId());
    }

    @Transactional 
    public ProjectResponse update(Long projectId, ProjectRequest request){
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Project not found"
            ));

        Set<User> members = validateMembers(request.membersIds());

        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setMembers(members);

        projectRepository.saveAndFlush(project);

        return getProject(projectId);
    }


    @Transactional
    public void delete(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"
                ));

        long tickets = ticketRepository
                .countByProjectReferenceId(projectId);

        long legacyTickets = ticketRepository
                .countByProjectReferenceIsNullAndProject(
                        project.getName()
                );

        if (tickets + legacyTickets > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a project with tickets"
            );
        }

        projectRepository.delete(project);
        projectRepository.flush();
    }


    private Set<User> validateMembers(List<Long> membersIds){
         if (membersIds == null || membersIds.contains(null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid project members"
            );
        }

        Set<Long> uniqueIds = new HashSet<>(membersIds);

        List<User> users = userRepository.findAllById(uniqueIds);

        if (users.size() != uniqueIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more users do not exist"
            );
        }

        return new HashSet<>(users);
    }

    @Transactional(readOnly = true)
    public List<Long> getMemberIds(Long projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"
                ));

        return project.getMembers()
                .stream()
                .map(User::getId)
                .sorted()
                .toList();
    }
}
