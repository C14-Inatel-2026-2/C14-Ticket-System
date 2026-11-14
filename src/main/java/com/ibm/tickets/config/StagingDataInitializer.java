
package com.ibm.tickets.config;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.model.User;
import com.ibm.tickets.model.UserRole;
import com.ibm.tickets.repository.ProjectRepository;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.repository.UserRepository;

/**
 * @author Rodrigo Fraga da Costa
 * StagingDataInitializer
 */
@Component
@Profile("staging")
@ConditionalOnProperty(
    name = "c14.staging.seed.enabled",
    havingValue = "true"
)
public class StagingDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedPassword;

    public StagingDataInitializer(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            TicketRepository ticketRepository,
            PasswordEncoder passwordEncoder,
            @Value("${c14.staging.seed.password}")
            String seedPassword) {

        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.ticketRepository = ticketRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedPassword = seedPassword;
    }

    /**
     * @author Rodrigo Costa
     * SeedUser
     * @param name
     * @param email
     * @param role
     */
    private record SeedUser(
            String name,
            String email,
            UserRole role) {
    }

    private static final List<SeedUser> USERS = List.of(
        new SeedUser("Alice", "alice@stage.example.com", UserRole.ADMIN),
        new SeedUser("Bruno", "bruno@stage.example.com", UserRole.PROJECT_MANAGER),
        new SeedUser("Carla", "carla@stage.example.com", UserRole.PROJECT_MANAGER),
        new SeedUser("Daniel", "daniel@stage.example.com", UserRole.DEVELOPER),
        new SeedUser("Elisa", "elisa@stage.example.com", UserRole.DEVELOPER),
        new SeedUser("Felipe", "felipe@stage.example.com", UserRole.DEVELOPER),
        new SeedUser("Helena", "helena@stage.example.com", UserRole.DEVELOPER),
        new SeedUser("Igor", "igor@stage.example.com", UserRole.TESTER),
        new SeedUser("Julia", "julia@stage.example.com", UserRole.TESTER),
        new SeedUser("Lucas", "lucas@stage.example.com", UserRole.TESTER),
        new SeedUser("Nina", "nina@stage.example.com", UserRole.REPORTER),
        new SeedUser("Pedro", "pedro@stage.example.com", UserRole.REPORTER)
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (seedPassword == null || seedPassword.length() < 8) {
            throw new IllegalStateException(
                "Staging seed password must have at least 8 characters"
            );
        }

        Map<String, User> users = createUsers();

        createProject(
            "STAGE - E-commerce",
            "Online store management.",
            List.of(
                "bruno@stage.example.com",
                "daniel@stage.example.com",
                "elisa@stage.example.com",
                "igor@stage.example.com"
            ),
            users
        );

        createProject(
            "STAGE - Mobile App",
            "Mobile application development.",
            List.of(
                "carla@stage.example.com",
                "felipe@stage.example.com",
                "helena@stage.example.com",
                "julia@stage.example.com"
            ),
            users
        );

        createProject(
            "STAGE - Internal API",
            "Internal API development and maintenance.",
            List.of(
                "bruno@stage.example.com",
                "daniel@stage.example.com",
                "felipe@stage.example.com",
                "lucas@stage.example.com"
            ),
            users
        );

        createProject(
            "STAGE - Quality Assurance",
            "Testing and quality assurance.",
            List.of(
                "carla@stage.example.com",
                "igor@stage.example.com",
                "julia@stage.example.com",
                "lucas@stage.example.com"
            ),
            users
        );

        createProject(
            "STAGE - Customer Support",
            "Customer requests and incidents.",
            List.of(
                "bruno@stage.example.com",
                "elisa@stage.example.com",
                "nina@stage.example.com",
                "pedro@stage.example.com"
            ),
            users
        );
    }

    private Map<String, User> createUsers() {

        for (SeedUser data : USERS) {

            if (userRepository.existsByEmailIgnoreCase(data.email())) {
                continue;
            }

            User user = new User(
                data.name(),
                data.email(),
                data.role()
            );

            user.setPasswordHash(
                passwordEncoder.encode(seedPassword)
            );

            userRepository.save(user);
        }

        return USERS.stream().collect(
            Collectors.toMap(
                SeedUser::email,
                data -> userRepository
                    .findByEmail(data.email())
                    .orElseThrow()
            )
        );
    }

    private void createProject(
            String name,
            String description,
            List<String> memberEmails,
            Map<String, User> users) {

        Project project = projectRepository
            .findFirstByNameOrderByIdAsc(name)
            .orElseGet(() -> {

                Project created = new Project(name, description);

                for (String email : memberEmails) {
                    created.getMembers().add(users.get(email));
                }

                return projectRepository.saveAndFlush(created);
            });

        createTickets(project, memberEmails, users);
    }

    private void createTickets(
            Project project,
            List<String> memberEmails,
            Map<String, User> users) {

        TicketPriority[] priorities = TicketPriority.values();

        TicketStatus[] statuses = {
            TicketStatus.OPEN,
            TicketStatus.IN_PROGRESS,
            TicketStatus.RESOLVED,
            TicketStatus.CLOSED
        };

        for (int i = 0; i < 4; i++) {

            String title = "Stage Test Ticket " + (i + 1);

            if (ticketRepository.existsByTitleAndProjectReference_Id(
                    title, project.getId())) {
                continue;
            }

            User assignee = users.get(
                memberEmails.get(i % memberEmails.size())
            );

            Ticket ticket = new Ticket(
                title,
                "Test ticket generated for the staging environment.",
                priorities[i % priorities.length]
            );

            ticket.setProjectReference(project);
            ticket.setAssigneeUser(assignee);
            ticket.setAssignee(assignee.getName());
            ticket.setStatus(statuses[i % statuses.length]);

            ticketRepository.save(ticket);
        }
    }
}
