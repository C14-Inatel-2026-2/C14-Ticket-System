
# C14 Ticket System — Requirements Specification

## 1. Introduction

This document defines the functional requirements (FR), non-functional requirements (NFR), and business rules (BR) of the C14 Ticket System.

The C14 Ticket System is a web-based application designed to manage software projects, tickets, users, and ticket activity histories.

The system is developed using Java, Spring Boot, Thymeleaf, PostgreSQL, and Docker, with automated testing and deployment through Jenkins, GitHub Container Registry (GHCR), and Railway.

### 1.1 Requirement Classification

- **Functional Requirements (FR):** Define the features, operations, and behaviors provided by the system.
- **Non-Functional Requirements (NFR):** Define technical constraints, quality attributes, and operational characteristics.
- **Business Rules (BR):** Define restrictions, validations, and conditions governing system operations.

---

## 2. Functional Requirements

### 2.1 Authentication and User Profiles

| ID | Requirement |
|---|---|
| FR01 | The system shall allow registered users to authenticate using their email address and password. |
| FR02 | The system shall allow authenticated users to log out, terminating their current session. |
| FR03 | The system shall restrict access to protected resources to authenticated users. |
| FR04 | The system shall allow authenticated users to view their profiles, including name, email, role, assigned tickets, completed tickets, and associated projects. |
| FR05 | The system shall allow users to retrieve registered user profiles by ID. |
| FR06 | The system shall provide a list of registered users, including their ID, name, email, and role. |
| FR07 | The system shall support the roles ADMIN, PROJECT_MANAGER, DEVELOPER, TESTER, and REPORTER. |
| FR08 | The system shall provide an activity calendar displaying generated activity information for the authenticated user. |

### 2.2 User Management

| ID | Requirement |
|---|---|
| FR09 | The system shall allow authorized users to create user accounts. |
| FR10 | The system shall allow authorized users to delete existing user accounts. |
| FR11 | The system shall allow user accounts to be created with a name, email, password, and role. |
| FR12 | The system shall provide a user management interface for listing, searching, creating, and deleting users. |
| FR13 | The system shall restrict user creation and deletion to ADMIN and PROJECT_MANAGER roles. |
| FR14 | The system shall allow authenticated users to view the registered user directory. |

### 2.3 Project Management

| ID | Requirement |
|---|---|
| FR15 | The system shall allow authorized users to create projects with a name, description, and assigned members. |
| FR16 | The system shall allow authenticated users to view registered projects. |
| FR17 | The system shall allow users to retrieve project information by ID. |
| FR18 | The system shall allow authorized users to edit project names, descriptions, and members. |
| FR19 | The system shall allow authorized users to delete projects that have no associated tickets. |
| FR20 | The system shall allow users to search projects by name or description. |
| FR21 | The system shall allow project members to be selected from registered users. |
| FR22 | The system shall allow authorized users to add or remove members from projects. |
| FR23 | The system shall display the number of tickets associated with each project. |
| FR24 | The system shall restrict project creation, editing, and deletion to ADMIN and PROJECT_MANAGER roles. |
| FR25 | The system shall provide the identifiers of users associated with a project. |

### 2.4 Ticket Management

| ID | Requirement |
|---|---|
| FR26 | The system shall allow authenticated users to create tickets associated with registered projects. |
| FR27 | The system shall allow users to view tickets and their details. |
| FR28 | The system shall allow users to update ticket information, including title, description, project, priority, and assignee. |
| FR29 | The system shall allow users to delete existing tickets. |
| FR30 | The system shall allow users to change ticket status. |
| FR31 | The system shall support the ticket statuses OPEN, IN_PROGRESS, RESOLVED, and CLOSED. |
| FR32 | The system shall support the ticket priorities LOW, MEDIUM, HIGH, and URGENT. |
| FR33 | The system shall allow tickets to be assigned to registered users. |
| FR34 | The system shall allow users to filter tickets by project, status, and priority. |
| FR35 | The system shall provide ticket listings ordered by creation date, with the most recent tickets first. |
| FR36 | The system shall verify that a ticket belongs to the specified project when retrieving, updating, or deleting it through project-scoped operations. |

### 2.5 Comments and Activity History

| ID | Requirement |
|---|---|
| FR37 | The system shall allow authenticated users to add comments to tickets. |
| FR38 | The system shall allow users to view comments associated with a ticket. |
| FR39 | The system shall record changes to ticket status, priority, and assignee. |
| FR40 | The system shall maintain a unified chronological history of ticket activities and comments. |
| FR41 | The system shall associate ticket comments and recorded changes with the responsible user's name. |
| FR42 | The system shall preserve historical status changes through the unified ticket activity mechanism. |

### 2.6 Application Access and Monitoring

| ID | Requirement |
|---|---|
| FR43 | The system shall provide web pages for authentication, project management, ticket management, and user profiles. |
| FR44 | The system shall provide a user management page accessible only to authorized roles. |
| FR45 | The system shall expose a health check endpoint at GET /health that returns OK. |

---

## 3. Non-Functional Requirements

### 3.1 Architecture and Maintainability

| ID | Requirement |
|---|---|
| NFR01 | The backend shall use Java 21 and Spring Boot. |
| NFR02 | The application shall follow a layered architecture separating controllers, services, repositories, entities, DTOs, configuration, and exception handling. |
| NFR03 | The application shall use Spring Data JPA and Hibernate for relational data persistence. |
| NFR04 | The application shall use Spring MVC and Thymeleaf for server-side web page rendering. |
| NFR05 | The application shall use JavaScript for client-side interactions with the REST API. |

### 3.2 Security and Authentication

| ID | Requirement |
|---|---|
| NFR06 | User passwords shall be stored as BCrypt hashes rather than plaintext values. |
| NFR07 | The system shall use HTTP sessions to maintain authenticated user state. |
| NFR08 | The system shall invalidate the current session when a user logs out. |
| NFR09 | Protected API endpoints shall return HTTP 401 Unauthorized when authentication is required but absent. |
| NFR10 | Protected browser routes shall redirect unauthenticated users to the login page. |
| NFR11 | Project management permissions shall be validated by the backend independently of the frontend interface. |
| NFR12 | User management permissions shall be validated by the backend independently of the frontend interface. |
| NFR13 | Authenticated users without sufficient permissions shall receive HTTP 403 Forbidden when attempting restricted operations. |

### 3.3 Data Validation and Integrity

| ID | Requirement |
|---|---|
| NFR14 | The application shall use Jakarta Bean Validation to validate incoming requests. |
| NFR15 | Invalid request data shall be rejected with appropriate HTTP error responses. |
| NFR16 | API validation errors shall provide structured responses containing information about invalid fields. |
| NFR17 | The database shall enforce relational integrity through primary keys, foreign keys, and defined constraints. |
| NFR18 | Database write operations shall use transactional boundaries to maintain consistency. |

### 3.4 Database and Persistence

| ID | Requirement |
|---|---|
| NFR19 | The application shall use PostgreSQL as its relational database. |
| NFR20 | Database schema changes shall be managed through versioned Flyway migrations. |
| NFR21 | Hibernate shall validate the database schema rather than automatically generate or modify it. |
| NFR22 | Application data shall be stored independently of the application container. |

### 3.5 Containerization and Deployment

| ID | Requirement |
|---|---|
| NFR23 | The application shall support containerized execution using Docker. |
| NFR24 | The Docker image shall use a multi-stage build to separate the Java build environment from the runtime environment. |
| NFR25 | The application container shall expose port 8080. |
| NFR26 | The local environment shall support execution through Docker Compose with separate application and PostgreSQL services. |
| NFR27 | The local PostgreSQL container shall use a persistent Docker volume. |
| NFR28 | The deployment pipeline shall publish Docker images to GitHub Container Registry (GHCR). |
| NFR29 | The deployment pipeline shall support deploying the application to Railway. |
| NFR30 | Docker image publication and Railway deployment stages shall be restricted to the main branch. |

### 3.6 Testing and Continuous Integration

| ID | Requirement |
|---|---|
| NFR31 | The project shall use JUnit for automated Java testing. |
| NFR32 | The project shall support isolated testing using Mockito. |
| NFR33 | The project shall contain automated tests for controllers, services, repositories, and request validation. |
| NFR34 | The project shall provide browser-level tests using Playwright. |
| NFR35 | The project shall support end-to-end tests involving application operations and database persistence. |
| NFR36 | Jenkins shall execute compilation, automated tests, and application packaging stages. |
| NFR37 | Jenkins shall collect and publish JUnit test reports. |
| NFR38 | Jenkins shall generate and publish JaCoCo code coverage reports. |
| NFR39 | Docker image publication shall occur after the preceding build and test stages complete successfully. |

### 3.7 Integration and Observability

| ID | Requirement |
|---|---|
| NFR40 | The backend shall expose REST API endpoints using HTTP and JSON. |
| NFR41 | The system shall use appropriate HTTP methods for resource creation, retrieval, modification, and deletion. |
| NFR42 | The application shall provide a health check endpoint for basic application availability verification. |
| NFR43 | The application shall generate logs that can be inspected during execution and deployment. |

---

## 4. Business Rules

### 4.1 Authentication and User Management

| ID | Business Rule |
|---|---|
| BR01 | Only ADMIN and PROJECT_MANAGER users may create or delete user accounts. |
| BR02 | Users cannot delete their own accounts. |
| BR03 | Users with assigned tickets cannot be deleted until those tickets are reassigned. |
| BR04 | User email addresses must be unique. |
| BR05 | New user email addresses are normalized to lowercase before persistence. |
| BR06 | User names must not be blank and must contain at most 100 characters. |
| BR07 | User email addresses must be valid and contain at most 150 characters. |
| BR08 | User passwords must contain between 8 and 72 characters when creating an account. |
| BR09 | Every registered user must have one of the supported system roles. |
| BR10 | Deleting a user removes their project memberships without deleting the projects. |

### 4.2 Project Management

| ID | Business Rule |
|---|---|
| BR11 | Only ADMIN and PROJECT_MANAGER users may create, edit, or delete projects. |
| BR12 | Project names must not be blank and must contain at most 100 characters. |
| BR13 | Project members must reference existing registered users. |
| BR14 | Invalid or nonexistent project member IDs must be rejected. |
| BR15 | Projects containing associated tickets cannot be deleted. |
| BR16 | Project management controls must only be displayed to authorized roles. |

### 4.3 Ticket Management

| ID | Business Rule |
|---|---|
| BR17 | Ticket titles must not be blank and must contain at most 150 characters. |
| BR18 | Ticket descriptions must not be blank. |
| BR19 | Every newly created ticket must reference an existing project. |
| BR20 | Ticket creation and modification requests must provide an existing assignee user ID. |
| BR21 | Ticket priorities must use one of the supported priority values. |
| BR22 | Ticket statuses must use one of the supported status values. |
| BR23 | Project-scoped ticket operations must reject tickets that do not belong to the specified project. |
| BR24 | Ticket comments must not be blank and must contain at most 5,000 characters. |

### 4.4 Activity History

| ID | Business Rule |
|---|---|
| BR25 | Changes to ticket status, priority, and assignee must be recorded when the previous and new values differ. |
| BR26 | Updating a ticket status to its current value must not generate a new activity record. |
| BR27 | Ticket activities and comments must be presented in chronological order. |
| BR28 | Tickets with RESOLVED or CLOSED status are considered completed when calculating user statistics. |

---

## 5. Implementation Traceability

This section identifies the main components responsible for implementing each requirement group.

| Requirement Group | Main Components |
|---|---|
| Authentication | `LoginController`, `AuthenticationService`, `AuthenticationInterceptor` |
| User Profiles | `UserController`, `UserService`, `UserRepository` |
| User Management | `UserController`, `UserService`, `UserPermissionInterceptor`, `CreateUserRequest` |
| User Roles | `UserRole`, `UserPermissionInterceptor`, `ProjectPermissionInterceptor` |
| Project Management | `ProjectController`, `ProjectWebController`, `ProjectService`, `ProjectRepository` |
| Ticket Management | `TicketController`, `TicketWebController`, `TicketService`, `TicketRepository` |
| Ticket Comments | `TicketCommentController`, `TicketCommentService`, `TicketCommentRepository` |
| Activity History | `TicketActivityController`, `TicketActivityService`, `TicketActivityRepository` |
| Database Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Schema Management | Flyway migrations |
| Authentication Security | BCrypt, HTTP sessions, authentication interceptors |
| Automated Testing | JUnit, Mockito, Spring Boot Test, Playwright |
| Code Coverage | JaCoCo |
| Continuous Integration | Jenkins |
| Containerization | Docker, Docker Compose |
| Container Registry | GitHub Container Registry |
| Deployment | Railway |
| Health Monitoring | `HealthController` |

---

## 6. Verification and Limitations

The requirements listed in this document describe behaviors and technical mechanisms implemented in the project's source code.

Implementation evidence does not automatically demonstrate successful execution, security compliance, or performance under load.

The following limitations apply:

- The user activity calendar currently uses generated data rather than recorded user contributions.
- The development administrator login and predefined demonstration credentials remain separate from the persisted-user authentication mechanism and require appropriate protection in public environments.
- Role-based authorization uses the role stored in the authenticated HTTP session.
- The user management interface currently supports account creation and deletion, but not account editing.
- No verified performance targets, concurrent-user limits, uptime guarantees, or recovery-time objectives have been established.
- The health check endpoint confirms a basic HTTP response but does not independently verify PostgreSQL connectivity.

These limitations are documented to distinguish implemented behavior from capabilities that have not been established.

---

## 7. References

- [C14 Ticket System Repository](https://github.com/C14-Inatel-2026-2/C14-Ticket-System)
- [Java Source Code](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets)
- [Automated Tests](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/test)
- [Database Migrations](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/resources/db/migration)
- [Jenkins Pipeline](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/blob/main/Jenkinsfile)
- [Dockerfile](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/blob/main/Dockerfile)
- [Docker Compose Configuration](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/blob/main/compose.yaml)
