package com.ibm.tickets.controller.Api;

import com.ibm.tickets.dto.ProjectResponse;
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
class ProjectController {
   
   @GetMapping
   public List<ProjectResponse> getProjects() {
    return List.of(
            new ProjectResponse(
                1L,
                "C14 Ticket System",
                "Sistema de gerenciamento de tickets.",
                12
            ),
            new ProjectResponse(
                2L,
                "OpenLift",
                "Aplicativo de gerenciamento de academias.",
                8
            ),
            new ProjectResponse(
                3L,
                "Portal Interno",
                "Portal de serviços e solicitações internas.",
                5
            )
        );
   }


}