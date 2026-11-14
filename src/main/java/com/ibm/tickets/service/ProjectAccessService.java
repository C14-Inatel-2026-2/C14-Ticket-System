
package com.ibm.tickets.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.ProjectRepository;

import jakarta.servlet.http.HttpSession;

/**
 * @author Rodrigo Costa
 * ProjectAccessService
 */
@Service
public class ProjectAccessService {

    private final ProjectRepository projectRepository;

    public ProjectAccessService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public boolean hasGlobalAccess(HttpSession session) {
        UserRole role = getRole(session);

        return role == UserRole.ADMIN
            || role == UserRole.PROJECT_MANAGER;
    }

    public Long getUserId(HttpSession session) {
        Object userId = session == null
            ? null
            : session.getAttribute("userId");

        if (userId instanceof Long id) {
            return id;
        }

        throw new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "User not authenticated"
        );
    }

    public void checkAccess(
            Long projectId,
            HttpSession session) {

        if (projectId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Project ID is required"
            );
        }

        if (!projectRepository.existsById(projectId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Project not found"
            );
        }

        if (hasGlobalAccess(session)) {
            return;
        }

        Long userId = getUserId(session);

        boolean isMember =
            projectRepository.existsByIdAndMembers_Id(
                projectId,
                userId
            );

        if (!isMember) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You do not have access to this project"
            );
        }
    }

    private UserRole getRole(HttpSession session) {
        if (session == null
                || session.getAttribute("loggedUser") == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not authenticated"
            );
        }

        Object roleValue = session.getAttribute("userRole");

        if (!(roleValue instanceof String roleName)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Invalid user role"
            );
        }

        try {
            return UserRole.valueOf(roleName);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Invalid user role"
            );
        }
    }
}
