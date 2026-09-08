package com.ibm.tickets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Rodrigo Fraga da Costa
 * Home
 */
@Controller
@RequestMapping("/")
public class Home {
    
    @GetMapping
    public String homePage(Model model){
        return "home";
    }
}
