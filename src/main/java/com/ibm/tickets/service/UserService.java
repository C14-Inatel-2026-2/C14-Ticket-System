package com.ibm.tickets.service;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ibm.tickets.dto.CreateUserRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.dto.UserOptionResponse;
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
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TicketRepository ticketRepository,
        PasswordEncoder passwordEncoder
    ){
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
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

    public List<UserOptionResponse> getAllUsers(){
        return userRepository.findAllByOrderByNameAsc()
            .stream()
            .map(user -> new UserOptionResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name()
            ))
            .toList();
    }

    @Transactional 
    public UserOptionResponse createUser(CreateUserRequest request){
        String name = request.name();
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmailIgnoreCase(email)){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already registered"
            );
        }

        User user = new User(name, email, request.role());

        user.setPasswordHash(
            passwordEncoder.encode(request.password())
        );

        try{
            User savedUser = userRepository.saveAndFlush(user);
            return toResponse(savedUser);
        } catch(DataIntegrityViolationException exception){
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Unable to create user. Email may already exist.",
                exception
            );
        }
    }

    @Transactional 
    public void deleteUser(Long userId, Long currentUserId){
        if(userId.equals(currentUserId)){
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You cannot delete your own account"
            );
        }

        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found"
                        ));
        
        if(ticketRepository.countByAssigneeUserId(userId) > 0){
             throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User has assigned tickets"
            );
        }

        userRepository.delete(user);
        userRepository.flush();
    }

    public List<TicketResponse> getAssignedTickets(Long userId){
        if(!userRepository.existsById(userId)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "User not found."
            );
        }

        return ticketRepository.findAllByAssigneeUserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(TicketResponse::from)
                    .toList();
    }

    private UserOptionResponse toResponse(User user){
        return new UserOptionResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().name()
        );
    }
}
