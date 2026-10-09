package com.ibm.tickets.controller.Web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Rodrigo Fraga da Costa
 * UserWebController
 */
@Controller
public class UserWebController {
    
    @GetMapping("/users/me")
    public String profile(){
        return "user-profile";
    }
}
