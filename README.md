# C14 Ticket System

A **web-based ticket management system** developed as part of the C14 project.

The application provides a centralized environment for creating, managing, and tracking tickets throughout their lifecycle. The project is built with **Java and Spring Boot**, following a structured development workflow with automated validation through **Jenkins CI**.

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
* [ ] Ticket comments
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
* [ ] Continuous Deployment

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


## Contributors

Before contributing to the project, please read the [Contribution Guidelines](docs/contributing.md).
