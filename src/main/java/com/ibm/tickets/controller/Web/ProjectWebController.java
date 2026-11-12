package com.ibm.tickets.controller.Web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * Web controller for project pages.
 * @author Rodrigo Costa
 */

@Controller
@RequestMapping("/project")
class ProjectWebController{

    @GetMapping
    public String getProjectPage(){
        return "projects";
    }

    @GetMapping("/new")
    public String createProjectPage() {
        return "project-form";
    }

    @GetMapping("/{projectId}/edit")
    public String editProjectPage(@PathVariable Long projectId) {
        return "project-form";
}
}