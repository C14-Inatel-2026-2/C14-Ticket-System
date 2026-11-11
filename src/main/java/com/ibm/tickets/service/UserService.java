package com.ibm.tickets.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.UserProfileResponse;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.User;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.UserRepository;

/**
 * @author Rodrigo Costa
 * UserService
 */
@Service 
@Transactional (readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public UserService(UserRepository userRepository, TicketRepository ticketRepository){
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    public UserProfileResponse getProfile(Long userId){

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND
                        ));
        
            long assigneeTickets = ticketRepository
                                    .countByAssigneeUserId(userId);
            long completedTickets = ticketRepository
                                    .countByAssigneeUserIdAndStatusIn(userId,
                                        List.of(TicketStatus.RESOLVED,
                                                TicketStatus.CLOSED
                                        ));

            
            List<String> projects = user.getProjects()
                                        .stream()
                                        .map(project -> project.getName())
                                        .sorted()
                                        .toList();
            return new UserProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                Math.toIntExact(assigneeTickets),
                Math.toIntExact(completedTickets),
                projects
            );

    }
}
