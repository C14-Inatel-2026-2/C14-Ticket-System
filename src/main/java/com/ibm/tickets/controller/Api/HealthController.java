package com.ibm.tickets.controller.Api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rodrigo Costa
 * HealthController
 */
@RestController
public class HealthController {
    
    @GetMapping ("/health")
    public String health(){
        return "OK";
    }
}
