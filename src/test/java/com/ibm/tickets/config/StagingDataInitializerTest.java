package com.ibm.tickets.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import com.ibm.tickets.model.*;
import com.ibm.tickets.repository.*;

/** @author Gabriel Guimaraes */
class StagingDataInitializerTest {
    private final UserRepository users = mock(UserRepository.class);
    private final ProjectRepository projects = mock(ProjectRepository.class);
    private final TicketRepository tickets = mock(TicketRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void invalidPasswordsFailBeforeAnyPersistenceAccess() {
        for (String password : new String[] {null, "", "1234567"}) {
            StagingDataInitializer initializer = new StagingDataInitializer(users, projects, tickets, encoder, password);
            assertThrows(IllegalStateException.class, () -> initializer.run(null));
        }
        verifyNoInteractions(users, projects, tickets, encoder);
    }

    @Test
    void seedsCompleteLinkedDataAndSecondRunDoesNotDuplicateOrOverwriteIt() {
        Map<String, User> userStore = new HashMap<>();
        Map<String, Project> projectStore = new HashMap<>();
        Map<String, Ticket> ticketStore = new HashMap<>();
        when(encoder.encode("password")).thenReturn("encoded-password");
        when(users.existsByEmailIgnoreCase(anyString())).thenAnswer(i -> userStore.containsKey(i.getArgument(0)));
        when(users.findByEmail(anyString())).thenAnswer(i -> Optional.ofNullable(userStore.get(i.getArgument(0))));
        when(users.save(any(User.class))).thenAnswer(i -> {
            User user = i.getArgument(0);
            userStore.put(user.getEmail(), user);
            return user;
        });
        when(projects.findFirstByNameOrderByIdAsc(anyString()))
                .thenAnswer(i -> Optional.ofNullable(projectStore.get(i.getArgument(0))));
        when(projects.saveAndFlush(any(Project.class))).thenAnswer(i -> {
            Project project = i.getArgument(0);
            ReflectionTestUtils.setField(project, "id", (long) projectStore.size() + 1);
            projectStore.put(project.getName(), project);
            return project;
        });
        when(tickets.existsByTitleAndProjectReference_Id(anyString(), anyLong()))
                .thenAnswer(i -> ticketStore.containsKey(i.getArgument(1) + ":" + i.getArgument(0)));
        when(tickets.save(any(Ticket.class))).thenAnswer(i -> {
            Ticket ticket = i.getArgument(0);
            ticketStore.put(ticket.getProjectReference().getId() + ":" + ticket.getTitle(), ticket);
            return ticket;
        });
        StagingDataInitializer initializer = new StagingDataInitializer(users, projects, tickets, encoder, "password");
        initializer.run(null);
        assertEquals(12, userStore.size());
        assertEquals(5, projectStore.size());
        assertEquals(20, ticketStore.size());
        assertEquals(UserRole.ADMIN, userStore.get("alice@stage.example.com").getRole());
        assertEquals(UserRole.PROJECT_MANAGER, userStore.get("bruno@stage.example.com").getRole());
        assertTrue(userStore.values().stream().allMatch(u -> "encoded-password".equals(u.getPasswordHash())));
        for (Project project : projectStore.values()) {
            assertEquals(4, project.getMembers().size());
            List<Ticket> projectTickets = ticketStore.values().stream()
                    .filter(t -> t.getProjectReference() == project).toList();
            assertEquals(4, projectTickets.size());
            assertEquals(Set.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED),
                    new HashSet<>(projectTickets.stream().map(Ticket::getStatus).toList()));
            for (Ticket ticket : projectTickets) {
                assertTrue(project.getMembers().contains(ticket.getUser()));
                assertEquals(ticket.getUser().getName(), ticket.getAssignee());
                int index = Integer.parseInt(ticket.getTitle().substring("Stage Test Ticket ".length())) - 1;
                assertEquals(TicketPriority.values()[index % TicketPriority.values().length], ticket.getPriority());
            }
        }
        User existingUser = userStore.get("alice@stage.example.com");
        existingUser.setPasswordHash("custom-password");
        Project existingProject = projectStore.values().iterator().next();
        existingProject.setDescription("custom description");
        initializer.run(null);
        assertEquals("custom-password", existingUser.getPasswordHash());
        assertEquals("custom description", existingProject.getDescription());
        verify(users, times(12)).save(any(User.class));
        verify(encoder, times(12)).encode("password");
        verify(projects, times(5)).saveAndFlush(any(Project.class));
        verify(tickets, times(20)).save(any(Ticket.class));
    }
}
