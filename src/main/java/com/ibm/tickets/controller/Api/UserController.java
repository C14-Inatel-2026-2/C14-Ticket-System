package com.ibm.tickets.controller.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.tickets.dto.UserActivityResponse;
import com.ibm.tickets.dto.UserProfileResponse;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * @author Rodrigo Fraga da Costa
 * UserController
 */
@RestController 
@RequestMapping("/api/users")
public class UserController {
    
    // Currently, the user profile data is hardcoded. 
    // Future implementation should fetch real user data from the database.
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(HttpSession session) {
        String userName = (String) session.getAttribute("loggedUser");

        if(userName == null){
            return ResponseEntity.status(401).build();
        }

        UserProfileResponse profile = new UserProfileResponse(userName,
            "admin@example.com",
            "Administrator",
            8,
            12,
            List.of("C14 Ticket System", "Projeto X", "Portal Interno")
            
        );
        return ResponseEntity.ok(profile);
    }

    // Current method generates random activity data for the past year. 
    // Future implementation should fetch real activity data from the database.
    @GetMapping("/me/activity")
    public ResponseEntity<List<UserActivityResponse>> getActivity(HttpSession session) {
        String userName = (String) session.getAttribute("loggedUser");

        if(userName == null){
            return ResponseEntity.status(401).build();
        }

        List<UserActivityResponse> activities = new ArrayList<>();
        Random random = new Random(42);

        LocalDate today = LocalDate.now();

        for(int i = 0; i < 365; i++){
            LocalDate date = today.minusDays(i);

            int count = random.nextDouble() < 0.45 ? 0 : random.nextInt(9);
            activities.add(new UserActivityResponse(date, count));
        }
        return ResponseEntity.ok(activities);
    }
}
