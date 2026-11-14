# C14 Ticket System

A **web-based ticket management system** developed as part of the C14 project.

The application provides a centralized environment for creating, managing, and tracking tickets throughout their lifecycle. The project is built with **Java and Spring Boot**, following a structured development workflow with automated validation through **Jenkins CI**.


## Live Application

The **C14 Ticket System** is publicly available on Railway.

🌐 **Access the application:** [C14 Ticket System](https://c14-ticket-system-production.up.railway.app)

The application uses Docker for containerization, PostgreSQL for data persistence, and Jenkins for its CI/CD pipeline, with Docker images published to GitHub Container Registry (GHCR) and deployed to Railway.

## Technologies

- Java
- Spring Boot
- Spring MVC
- Spring Data JPA
- Thymeleaf
- HTML / CSS
- PostgreSQL
- Flyway
- Maven
- JUnit
- Mockito
- Docker / Docker Compose
- Jenkins CI
- Git / GitHub

## Project Structure

The project follows a layered architecture:

```text
src/
    ├── main/
    │   ├── java/com/ibm/tickets/
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── exception/
    │   │   ├── model/
    │   │   ├── repository/
    │   │   └── service/
    │   │
    │   └── resources/
    │       ├── db/migration/
    │       ├── static/
    │       └── templates/
    │
    └── test/
        └── java/com/ibm/tickets/

```
The application is organised into separate layers for controllers, services, repositories, models, DTOs, exceptions, and configuration.



## Project Status

### Core Application

* [x] Ticket creation
* [x] Ticket listing
* [x] Ticket details
* [x] Ticket editing
* [x] Ticket deletion
* [x] Ticket status management
* [x] Ticket comments
* [x] Tickets status history
* [x] User authentication

### Database

* [x] PostgreSQL database
* [x] Hibernate / Spring Data JPA integration
* [x] Flyway database migrations
* [x] Docker Compose database environment

### DevOps & Quality

* [x] GitHub branch protection
* [x] Pull Request workflow
* [x] Jenkins CI pipeline
* [x] Automated build validation
* [x] Automated tests
* [x] Application containerization
* [x] Continuous Deployment

## Ticket Management

Tickets are persisted in PostgreSQL and accessed through Spring Data JPA.

The application supports the following ticket operations:

- Creating tickets
- Listing tickets
- Viewing ticket details
- Editing ticket information
- Deleting tickets
- Managing ticket status
- Setting ticket priority
- Tracking ticket status history

The web interface uses Thymeleaf templates for server-side page rendering and JavaScript for interactions with the REST API.

## Authentication

The application provides a login page and requires authentication before accessing the ticket management system.

Users can log in using the available credentials and log out through the application.

Authentication is handled using HTTP sessions and an interceptor that restricts access to protected routes.

## Database

The application uses PostgreSQL as its relational database.

Database schema changes are managed using Flyway migrations located in:

    src/main/resources/db/migration/

The project currently includes migrations for:

- Tickets
- Project and assignee fields
- Ticket status history

Spring Data JPA is used to provide database access through repositories.

## Testing

The project uses JUnit and Spring Boot Test for automated testing.

Tests cover different layers and components of the application, including:

- Controllers
- Services
- Repositories
- DTO validation
- Authentication
- Web controllers

Mockito is used for isolated unit tests with mocked dependencies.

Integration tests are also used where interaction with the application context or database is required.

## Running the Application

### Prerequisites

The following tools are required:

- Java
- Maven
- Docker
- Docker Compose

### Using Docker Compose

Start the application and PostgreSQL database with:

    docker compose up -d

The application is available at:

    http://localhost:8080

To stop the containers:

    docker compose down

### Running Tests

Run all automated tests with:

    mvn test

## Development Workflow

The project uses GitHub Pull Requests for collaborative development.

Development work should be performed in feature branches rather than directly on the `main` branch.

Feature branches should follow a descriptive naming convention, such as:

    feature/<feature-name>

Changes are submitted through Pull Requests and reviewed by another team member before being merged into the main branch.

Jenkins CI is used to automatically validate builds and tests.

## Refactorings

### Gabriel Guimaraes — seleção dos filtros de tickets

- **Smell** — `TicketService.findAll` misturava seleção de consultas e conversão para DTO.
- **Refactoring aplicado** — Extract Method para `findTickets`.
- **Por quê** — separar a escolha da consulta da apresentação da resposta, preservando os quatro cenários de filtro e a ordenação.
- **PR** — [#54](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/54).
- **Testes** — `mvn verify` com 97 testes na base e 99 após a extração, sem falhas. Os 10 testes de `TicketService` também passaram antes da extração.
- **Pipeline** — o Jenkins da base estava em `error` no início. Conferir os checks do PR para comprovar a validação no CI.


### Rodrigo Fraga da Costa — Removing legacy code

#### Smell
- **Duplicate Code:** Ticket changes were tracked through separate history mechanisms.
- **Obsolete Code:** The legacy `TicketStatusHistory` implementation was redundant.

#### Refactor Applied
- Introduced `TicketActivity` to centralize ticket activity tracking.
- Unified status, priority, assignee changes, and comments into a chronological timeline.
- Replaced legacy history recording with `TicketActivityService`.
- Removed obsolete entities, DTOs, repositories, and endpoints.
- Added Flyway migrations V8 and V9.

#### Why
- Reduce code duplication.
- Improve maintainability and consistency.
- Preserve historical data while simplifying the architecture.

#### Test
- Unit tests for activity recording and ordering.
- REST API tests for `GET /api/tickets/{id}/activities`.
- Database migration tests to ensure historical data preservation.

#### PR
- [PR #51 — Unified Activity History](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/51)
- [PR #52 — Legacy Status History Refactoring](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/52)


### Gabriel Guimaraes — mapeamento da timeline

- **Smell** — `TicketActivityService` montava respostas com sete argumentos posicionais, constantes e campos nulos, misturando consulta e representação.
- **Refactoring aplicado** — Move Function para `TicketActivityResponse.fromActivity` e `fromComment`.
- **Por quê** — concentrar a conversão no DTO, seguindo `TicketResponse.from`, e deixar o serviço responsável por consultar e ordenar os eventos. O conteúdo da resposta e a ordenação foram preservados.
- **PR** — [#55](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/55).
- **Testes** — `mvn verify` com 97 testes na base e 100 após a mudança, sem falhas. Os três novos testes cobrem todos os campos, valores nulos, datas e texto literal.
- **Pipeline** — o Jenkins da base estava em `error` no início. Conferir os checks do PR para comprovar a validação no CI.

### Mariana Barude Pina — Centralizing Ticket-Project Validation

- **Smell** — Duplicate Code: `TicketController.validateTicketProject` and `TicketWebController.findTicketInProject` implemented the same validation logic to verify whether a ticket belonged to the requested project.
- **Refactoring Applied** — Move Method: centralized the validation logic in `TicketService.findByIdInProject`.
- **Why** — eliminate duplicated business logic across controllers by moving the responsibility to the service layer, ensuring consistent behavior for both REST and Web interfaces. A dedicated `TicketNotInProjectException` was introduced to represent tickets that do not belong to the requested project.
- **Tests** — `mvn verify` with 118 tests executed, 0 failures, 0 errors, and 0 skipped.
- **PR** — [#63: Centralize ticket project validation](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/63).

### Mariana Barude Pina — Centralizing Project Lookup

- **Smell** — Duplicate Code: `ProjectService.update`, `ProjectService.delete`, and `ProjectService.getMemberIds` contained duplicated logic for retrieving a project by ID and handling cases where the project does not exist.
- **Refactoring Applied** — Extract Method: extracted the common lookup logic into the private method `ProjectService.findProjectById`.
- **Why** — eliminate duplicated code and centralize the responsibility for retrieving projects and handling not-found scenarios, improving maintainability and ensuring consistent behavior across service methods.
- **Tests** — `mvn verify` with 121 tests executed, 0 failures, 0 errors, and 0 skipped.
- **PR** — [#64: Centralize project lookup](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/64).

## AI Usage

### Models and Tools Used

**ChatGPT (OpenAI)** was used as a supporting tool during the development, code review, debugging, and documentation of the C14 Ticket System.

### Purpose of Use

The tool assisted with the following activities:

- **Development:** suggestions for implementing frontend and backend features.
- **Debugging:** investigating compilation errors, validation issues, and integration problems.
- **Automated Testing:** assistance in designing and implementing test cases.
- **Code Review:** analyzing pull requests and identifying potential inconsistencies.
- **Version Control:** organizing commits and providing guidance on branch integration.
- **Documentation:** assisting with technical descriptions of features and pull requests.

### Real Prompt Examples and Evaluation of AI Responses

The following examples demonstrate how AI was used throughout the project, including suggestions that were accepted, modified, or rejected.

The original prompts were written in Portuguese and have been translated into English for documentation purposes.

#### Example 1 — Debugging After Branch Integration

**Prompt:**

> I was testing an invalid date and noticed a problem: the list of available project assignees disappeared.

**Result:** ChatGPT helped investigate the access control changes introduced in PR #76, identifying that available ticket assignees were now filtered based on project membership.

**Evaluation:** **Accepted and applied.** The suggestion to verify project membership was followed. Adding the user to the project's members restored her availability as a ticket assignee in the form.

#### Example 2 — Debugging Outdated CSS Styles

**Prompt:**

> I made changes to the CSS files, but the updated styles are not appearing when I run the application. How can I identify and fix this issue?

**Result:** ChatGPT assisted in investigating the execution environment and suggested potential causes related to browser caching and outdated Docker images. The proposed troubleshooting steps included rebuilding the containers using commands such as `docker compose up -d --build` and performing a hard refresh in the browser.

**Evaluation:** **Accepted and applied.** The troubleshooting guidance helped identify the issue and restore the updated styles without requiring changes to the CSS implementation itself.

#### Example 3 — Organizing Version Control

**Prompt:**

> Provide the git add commands to organize the following changes into separate commits for backend, database, frontend, and tests:

The prompt included the output of `git status`, listing modified files across the different application layers, including:

- `src/main/java/com/ibm/tickets/dto/TicketRequest.java`
- `src/main/java/com/ibm/tickets/dto/TicketResponse.java`
- `src/main/java/com/ibm/tickets/model/Ticket.java`
- `src/main/java/com/ibm/tickets/service/TicketService.java`
- Frontend templates, JavaScript, and CSS files.
- Controller, DTO, repository, and service test files.
- `src/main/resources/db/migration/V13__add_ticket_due_date.sql`

**Result:** ChatGPT suggested dividing the changes into four commits according to their responsibilities: database, backend, frontend, and tests.

**Evaluation:** **Accepted.** The commits were created separately, followed by integration with the updated `main` branch and automated test execution.

#### Example 4 — UI Improvement Suggestion

**Context:** Development and review of the **My Tickets** feature, implemented on the user profile page (PR #74).

**AI Suggestion:** Apply additional CSS modifications to improve the visual presentation of the My Tickets section.

**Result:** After reviewing the proposed changes, the decision was made not to implement them, preserving the existing interface design.

**Evaluation:** **Rejected.** The existing implementation already met the project's visual and functional requirements. The proposed modifications were considered unnecessary, and maintaining consistency with the rest of the application was prioritized.

#### Example 5 — Implementing Ticket Due Dates

**Prompt:**

> The due date is not displayed anywhere except in the creation and editing forms. I was able to create and edit tickets correctly, but now we need to make the date visible, record any modifications in the activity history, and display a warning when a ticket is overdue.

**Result:** ChatGPT suggested changes to Thymeleaf templates, JavaScript, CSS, and backend services to display ticket deadlines, highlight overdue tickets, and record due date changes in the activity history.

**Evaluation:** **Accepted and adapted.** The proposed changes were reviewed and integrated into the existing application architecture. The functionality was subsequently verified through manual testing and automated tests.

### AI Usage Workflow

During development, ChatGPT was used as an interactive support tool. Questions, implementation difficulties, and unexpected errors were presented to the tool, which provided explanations, code suggestions, and possible solutions.

The suggestions were reviewed, adapted when necessary, and validated through automated and manual testing.

AI was also used to support the analysis of changes introduced by other team members, particularly when identifying potential incompatibilities between features developed in parallel.

**AI was not responsible for designing or defining the system architecture.** The architectural planning and fundamental technical decisions were made entirely by the development team.

### Manually Performed Activities

AI usage did not replace the team's responsibility for planning, implementing, testing, and validating the application.

The following activities were performed manually:

- Planning and defining the entire system architecture, including the separation of responsibilities between layers and the organization of application components.
- Selecting technologies and development tools, including Java, Spring Boot, Thymeleaf, PostgreSQL, Flyway, Maven, Docker, and Jenkins.
- Defining system features, business rules, entities, and database relationships.
- Integrating and adapting code to the existing project architecture.
- Executing automated tests and investigating failures.
- Performing manual tests on forms, overdue indicators, and ticket activity history.
- Organizing commits, managing branches, and resolving integration issues.
- Reviewing pull requests and making decisions regarding approvals and merges.
- Critically evaluating AI-generated suggestions, including accepting, modifying, or rejecting them.


## Requirements

The C14 Ticket System's functional and non-functional requirements are documented separately.

The specification covers ticket management, project management, user authentication, role-based access control, security, persistence, and system quality attributes.

📄 **[Functional and Non-Functional Requirements](docs/requirements.md)**


## User Stories

The C14 Ticket System's user stories are documented in the following document:

📄 **[User Stories](docs/user-stories.md)**


## Jenkins Jobs

The C14 Ticket System's Jenkins pipeline are documented in the following document:

📄 **[Jenkins](docs/jenkins-jobs.md)**

## Agile Development

The C14 Ticket System's Agile Development method are documented in the following document:

📄 **[Agile Development](docs/agile-development.md)**


## Contributors

Before contributing to the project, please read the [Contribution Guidelines](docs/contributing.md).
