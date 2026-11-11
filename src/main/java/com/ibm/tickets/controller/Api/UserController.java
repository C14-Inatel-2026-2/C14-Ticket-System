package com.ibm.tickets.controller.Api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.tickets.dto.UserActivityResponse;
import com.ibm.tickets.dto.UserProfileResponse;
import com.ibm.tickets.service.UserService;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author Rodrigo Fraga da Costa
 * UserController
 */
@RestController 
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    // Currently, the user profile data is hardcoded. 
    // Future implementation should fetch real user data from the database.
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(HttpSession session) {
        
        if (Boolean.TRUE.equals(
            session.getAttribute("globalAdmin"))) {

            return ResponseEntity.ok(
                    new UserProfileResponse(
                            "Admin",
                            "admin@localhost",
                            "ADMIN",
                            0,
                            0,
                            List.of()
                    )
            );
        }
        
        Object userId = session.getAttribute("userId");

        if(!(userId instanceof Long id)){
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userService.getProfile(id));
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

    @GetMapping("/{userId}")
    public UserProfileResponse getUserProfile(@PathVariable Long userId){
        return userService.getProfile(userId);
    }
}
