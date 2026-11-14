# C14 Ticket System — Requirements Specification

## 1. Scope and evidence

This document describes **implemented and directly verifiable behavior** in the `main` branch of [C14 Ticket System](https://github.com/C14-Inatel-2026-2/C14-Ticket-System), inspected on **November 13, 2026**. It is a descriptive specification of the current codebase, not a list of proposed features or performance targets.

- **FR** — Functional Requirement: a supported system behavior.
- **NFR** — Non-Functional Requirement: an implemented technical constraint or quality mechanism.
- **BR** — Business Rule: a specific validation or restriction enforced by the system.
- **Evidence** identifies the implementation or automated tests present in the repository; it does **not** imply that a test was executed during preparation of this document.
- Evidence links point to the repository's `main` branch. Subsequent local changes or unmerged branches are outside the verified scope.

## 2. Functional requirements

### 2.1 Authentication and user profiles

| ID | Requirement | Implementation evidence |
|---|---|---|
| FR01 | The system supports sign-in using an email address and password for persisted users. | `LoginController`, `AuthenticationService` |
| FR02 | The system supports sign-out by invalidating the current HTTP session. | `LoginController` (`POST /logout`) |
| FR03 | The system restricts configured ticket, project, user-profile, and API routes to authenticated sessions. | `AuthenticationInterceptor`, `WebConfig` |
| FR04 | The system provides a profile for the authenticated user with name, email, role, assigned-ticket count, completed-ticket count, and associated project names. | `UserController` (`GET /api/users/me`), `UserService` |
| FR05 | The system provides a user profile lookup by ID. | `UserController` (`GET /api/users/{userId}`) |
| FR06 | The system lists registered users with their ID, name, email, and role. | `UserController` (`GET /api/users`), `UserService` |
| FR07 | The system represents users with the roles `ADMIN`, `PROJECT_MANAGER`, `DEVELOPER`, `TESTER`, and `REPORTER`. | `UserRole`, `User` |
| FR08 | The system exposes an activity-calendar API for the signed-in user; **its current response is synthetic**, generated from a fixed-seed random sequence, not persisted user actions. | `UserController` (`GET /api/users/me/activity`) |

### 2.2 Project management

| ID | Requirement | Implementation evidence |
|---|---|---|
| FR09 | The system lists projects with ID, name, description, and ticket count. | `ProjectController` (`GET /api/projects`), `ProjectService`, `ProjectRepository` |
| FR10 | The system retrieves a project by ID. | `ProjectController` (`GET /api/projects/{projectId}`) |
| FR11 | The system creates a project with a name, description, and a list of existing user IDs as members. | `ProjectController` (`POST /api/projects`), `ProjectService` |
| FR12 | The system updates a project's name, description, and membership. | `ProjectController` (`PUT /api/projects/{projectId}`), `ProjectService` |
| FR13 | The system deletes a project only when it has no associated tickets, including tickets identified by its legacy project-name reference. | `ProjectController` (`DELETE /api/projects/{projectId}`), `ProjectService` |
| FR14 | The system returns the IDs of users associated with a project. | `ProjectController` (`GET /api/projects/{projectId}/members`) |
| FR15 | The projects page supports searching the loaded project catalog by name or description. | `templates/projects.html` |
| FR16 | The project form supports searching users by name or email, adding members, and removing selected members. | `templates/project-form.html`, `static/js/project-form.js` |

### 2.3 Ticket management

| ID | Requirement | Implementation evidence |
|---|---|---|
| FR17 | The system creates a ticket with title, description, project ID, assignee user ID, assignee name field, and priority. | `TicketController` (`POST /api/tickets`), `TicketRequest`, `TicketService` |
| FR18 | The system lists tickets, optionally filtering by project ID, status, and/or priority. | `TicketController` (`GET /api/tickets`), `TicketService` |
| FR19 | Ticket lists returned from the repository are ordered by creation date in descending order. | `TicketRepository` query methods |
| FR20 | The system retrieves a ticket by ID while checking that it belongs to the requested project. | `TicketController` (`GET /api/tickets/{id}?projectId=...`), `TicketService.findByIdInProject` |
| FR21 | The system updates a ticket's title, description, project, assignee, and priority. | `TicketController` (`PUT /api/tickets/{id}?projectId=...`), `TicketService` |
| FR22 | The system updates a ticket's status through a dedicated endpoint. | `TicketController` (`PATCH /api/tickets/{id}/status?projectId=...`) |
| FR23 | The system deletes a ticket from its specified project. | `TicketController` (`DELETE /api/tickets/{id}?projectId=...`) |
| FR24 | The system supports the ticket statuses `OPEN`, `IN_PROGRESS`, `RESOLVED`, and `CLOSED`. | `TicketStatus` |
| FR25 | The system supports the ticket priorities `LOW`, `MEDIUM`, `HIGH`, and `URGENT`. | `TicketPriority` |
| FR26 | The system associates tickets with persisted projects and users. | `Ticket`, `TicketService`, Flyway V6 and V10 |
| FR27 | The system provides web pages for project ticket lists and ticket creation, details, and editing. | `TicketWebController`, templates |

### 2.4 Comments and activity history

| ID | Requirement | Implementation evidence |
|---|---|---|
| FR28 | The system lists comments associated with a ticket. | `TicketCommentController` (`GET /api/tickets/{ticketId}/comments`) |
| FR29 | An authenticated user can add a comment to a ticket; the comment records the session user as author. | `TicketCommentController` (`POST /api/tickets/{ticketId}/comments`), `TicketCommentService` |
| FR30 | The system records changes to ticket assignee, priority, and status when their values change. | `TicketService`, `TicketActivityService.record` |
| FR31 | The system provides a unified chronological activity list combining ticket activities and comments. | `TicketActivityController` (`GET /api/tickets/{ticketId}/activities`), `TicketActivityService` |
| FR32 | The system preserves legacy status-history records by migrating them into the unified activity table. | Flyway `V9__migrate_status_history_to_activities.sql` |

### 2.5 Operational interface

| ID | Requirement | Implementation evidence |
|---|---|---|
| FR33 | The system serves a health-response endpoint at `GET /health`, returning `OK`. | `HealthController` |
| FR34 | The system exposes browser pages for login, projects, ticket operations, and user profiles. | MVC controllers and Thymeleaf templates |

## 3. Non-functional requirements

The following statements describe **implemented mechanisms and constraints**, not unverified service-level objectives.

| ID | Category | Requirement / verified technical constraint | Implementation evidence |
|---|---|---|---|
| NFR01 | Runtime | The backend is built for Java 21. | `pom.xml`, `Dockerfile` (Eclipse Temurin 21) |
| NFR02 | Architecture | The Java backend separates controllers, services, repositories, entities, DTOs, configuration, and exception handling into packages. | `src/main/java/com/ibm/tickets/` |
| NFR03 | Integration | The application exposes HTTP REST endpoints that accept and return JSON representations. | API controllers and DTOs |
| NFR04 | Web interface | HTML pages are served with Spring MVC / Thymeleaf, with client-side JavaScript for API-driven interactions. | MVC controllers, `templates/`, `static/js/` |
| NFR05 | Authentication | Protected routes are checked against an HTTP session; unauthenticated API calls to intercepted paths receive HTTP `401`, while intercepted browser routes redirect to `/`. | `AuthenticationInterceptor`, `WebConfig` |
| NFR06 | Password storage | Database-user authentication checks passwords against stored encoded hashes using `PasswordEncoder`; the application provides a BCrypt encoder configuration. | `AuthenticationService`, `PasswordConfig` |
| NFR07 | Session handling | Successful login creates or reuses a session and changes its session ID when one already exists; logout invalidates it. | `LoginController` |
| NFR08 | Input validation | The API uses Jakarta Bean Validation for ticket, project, comment, and status request DTOs. | `TicketRequest`, `ProjectRequest`, `TicketCommentRequest`, `TicketStatusRequest` |
| NFR09 | Error reporting | Validation errors are returned as HTTP `400` with a structured `ProblemDetail` response containing field errors; missing tickets and mismatched ticket/project references are handled as HTTP `404`. | `ApiExceptionHandler` |
| NFR10 | Database | Persistent application entities are mapped to PostgreSQL through Spring Data JPA / Hibernate. | Entities, repositories, `application.properties` |
| NFR11 | Database schema | Schema changes are applied through versioned Flyway SQL migrations; Hibernate is configured to validate rather than generate the schema. | `db/migration/V1`–`V12`, `application.properties` |
| NFR12 | Data integrity | Entity relationships and database foreign keys link tickets to projects/users, comments/activities to tickets, and members to projects. | JPA entities, Flyway migrations |
| NFR13 | Transactions | Service-layer database writes use Spring `@Transactional` boundaries for ticket and project mutations. | `TicketService`, `ProjectService` |
| NFR14 | Containerization | The application is packaged with a multi-stage Dockerfile: Java 21 JDK to build and Java 21 JRE to run; container port `8080` is declared. | `Dockerfile` |
| NFR15 | Local deployment | Docker Compose defines an application container and a separate PostgreSQL 17 container, using a named database volume and a database health check. | `compose.yaml` |
| NFR16 | CI | Jenkins defines checkout, compile, test, package, and Docker build/push stages. | `Jenkinsfile` |
| NFR17 | Test reporting | Jenkins publishes JUnit XML results and archives/publishes JaCoCo coverage reports. | `Jenkinsfile` |
| NFR18 | Artifact distribution | On the `main` branch, Jenkins builds and pushes images tagged with the build number and `latest` to GHCR. | `Jenkinsfile` |
| NFR19 | Deployment automation | On the `main` branch, Jenkins contains a stage using a Railway token and Railway CLI to request a redeploy of the configured service. | `Jenkinsfile` |
| NFR20 | Test infrastructure | The repository contains JUnit controller/service/repository tests, Playwright browser tests, and a separately enabled end-to-end integration test. | `src/test/java/`, `docs/local-browser-tests.md` |
| NFR21 | Health endpoint | An HTTP health endpoint is available for basic application response checks; it does not itself establish database health. | `HealthController` |

## 4. Business rules and validations

| ID | Rule | Implementation evidence |
|---|---|---|
| BR01 | A project's name must be nonblank and at most 100 characters. | `ProjectRequest` |
| BR02 | A project request must supply a non-null list of non-null member IDs; unknown member IDs are rejected with HTTP `400`. | `ProjectRequest`, `ProjectService.validateMembers` |
| BR03 | A project cannot be deleted while tickets reference it; the service responds with HTTP `409`. | `ProjectService.delete` |
| BR04 | A ticket title is required and limited to 150 characters; its description is required. | `TicketRequest` |
| BR05 | A ticket request requires project ID, assignee user ID, priority, and an assignee text field (at most 100 characters). | `TicketRequest` |
| BR06 | Ticket creation/updates resolve the assigned user by ID; an unknown user ID is rejected with HTTP `400`. | `TicketService.assignUser` |
| BR07 | Ticket access and update/delete operations that take a project ID verify the ticket belongs to that project. | `TicketController`, `TicketService.findByIdInProject` |
| BR08 | A status update requires a non-null status; updating to the same status does not create a new activity record. | `TicketStatusRequest`, `TicketService.updateStatus` |
| BR09 | A ticket comment must be nonblank and at most 5,000 characters. | `TicketCommentRequest` |
| BR10 | Activity records are not created when the old and new tracked values are equal. | `TicketActivityService.record` |
| BR11 | `RESOLVED` and `CLOSED` statuses count as completed tickets in user profiles. | `UserService.getProfile` |

## 5. Verified limitations and scope exclusions

The following points are **not documented as implemented requirements** because they are absent from the inspected `main` branch or cannot be demonstrated from repository contents alone:

1. **Role-based project authorization:** `UserRole` exists and roles are stored in the session, but the inspected `WebConfig` has no project permission interceptor. The current project REST controller does not itself restrict modifications to `ADMIN` and `PROJECT_MANAGER`.
2. **User account creation/deletion:** The inspected `UserController` only exposes GET operations, and no user-management page or user permission interceptor is present in `main`.
3. **Enforced password policy for newly created users:** There is no user-registration API in the inspected branch.
4. **Real user activity calendar:** `GET /api/users/me/activity` generates synthetic activity counts, not actual persisted contributions.
5. **Deployment safety:** The inspected `LoginController` includes a hard-coded `admin/admin` login. Its declared `c14.dev-admin.enabled` property is not checked in that login branch. `DevUsersPasswordInitializer` also initializes demonstration accounts with the password `admin` when their password hash is null. These are security limitations, not security requirements proven satisfied.
6. **Response-time guarantees, concurrent-user capacity, monthly uptime, recovery time, and accessibility conformance:** No threshold or supporting measurement is established by the inspected code or committed results; none is claimed here.
7. **Successful production deploy and successful test runs:** The Railway deployment stage and tests are present, and the README advertises a live application, but the source files alone do not independently prove runtime health, current availability, or the results of a specific pipeline execution.

If corresponding changes are present in another branch or only in the local workspace, verify and merge them before marking them as implemented in this specification.

## 6. Repository evidence index

- [REST controllers](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/controller/Api)
- [Web controllers](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/controller/Web)
- [Services](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/service)
- [DTOs and validation](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/dto)
- [Entities and enums](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/model)
- [Persistence repositories](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/repository)
- [Authentication and web configuration](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/java/com/ibm/tickets/config)
- [Flyway migrations](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/main/resources/db/migration)
- [Automated tests](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/tree/main/src/test/java/com/ibm/tickets)
- [Jenkins pipeline](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/blob/main/Jenkinsfile)
- [Dockerfile](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/blob/main/Dockerfile)
- [Docker Compose](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/blob/main/compose.yaml)

---

**Status convention:** Listed FRs, NFRs, and BRs are **implemented in source**. This means the supporting code or configuration exists; it is not a claim that all acceptance tests, load tests, security audits, or live infrastructure checks have passed.
