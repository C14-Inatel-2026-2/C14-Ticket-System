package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.ProjectResponse;
import com.ibm.tickets.service.ProjectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for project operations.
 * @author Rodrigo Costa
 */

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
   private final ProjectService projectService;

   public ProjectController(ProjectService projectService) {
      this.projectService = projectService;
   }
   
   @GetMapping
   public List<ProjectResponse> getProjects() {
      return projectService.getProjects();
   }
}
