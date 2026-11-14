package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.ProjectRequest;
import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.service.ProjectAccessService;
import com.ibm.tickets.service.ProjectService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for project operations.
 * @author Rodrigo Costa
 */

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
   private final ProjectService projectService;
   private final ProjectAccessService projectAccessService;

   public ProjectController(ProjectService projectService, ProjectAccessService projectAccessService) {
      this.projectService = projectService;
      this.projectAccessService = projectAccessService;
   }
   
   @GetMapping
   public List<ProjectResponse> getProjects(HttpSession session) {

      if(projectAccessService.hasGlobalAccess(session)){
         return projectService.getProjects();
      }

      Long userId = projectAccessService.getUserId(session);

      return projectService.getProjectsByUser(userId);
   }

   @GetMapping("/{projectId}")
   public ProjectResponse getProject(@PathVariable Long projectId, HttpSession session) {

      projectAccessService.checkAccess(projectId, session);

      return projectService.getProject(projectId);
   }

   @PostMapping 
   @ResponseStatus (HttpStatus.CREATED)
   public ProjectResponse create(@Valid @RequestBody ProjectRequest request){
      return projectService.create(request);
   }

   @PutMapping("/{projectId}")
   public ProjectResponse update(@PathVariable Long projectId,
         @Valid @RequestBody ProjectRequest request){
      return projectService.update(projectId, request);
   }


   @DeleteMapping("/{projectId}")
   @ResponseStatus(HttpStatus.NO_CONTENT)
   public void deleteProject(
         @PathVariable Long projectId) {

      projectService.delete(projectId);
   }


   @GetMapping ("/{projectId}/members")
   public List<Long> getMembersIds(@PathVariable Long projectId, HttpSession session){

      projectAccessService.checkAccess(projectId, session);
      
      return projectService.getMemberIds(projectId);
   }
}
