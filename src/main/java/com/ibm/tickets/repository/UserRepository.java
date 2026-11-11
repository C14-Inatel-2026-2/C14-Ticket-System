package com.ibm.tickets.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.tickets.model.User;

/**
 * @author Rodrigo Costa
 * UserRepository
 */
public interface UserRepository extends JpaRepository<User, Long>{
    
    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByNameAsc();

    @Override 
    @EntityGraph(attributePaths = "projects")
    Optional<User> findById(Long id);
}
