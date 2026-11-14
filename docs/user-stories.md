# User Stories — C14 Ticket System 

This document links the application's core user stories to GitHub work items, pull requests, implementation components, and automated tests. Acceptance criteria follow the **Given / When / Then** format. The stories describe expected behavior; references to tests identify code found on `main` and do not replace actual CI execution.

## Kanban Tracking

- **Organization projects (Kanban board directory):** [C14-Inatel-2026-2 — GitHub Projects](https://github.com/orgs/C14-Inatel-2026-2/projects).
- **Repository issues:** [C14 Ticket System — Issues](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues?q=is%3Aissue).
- **Pull requests:** [C14 Ticket System — Pull Requests](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pulls?q=is%3Apr).

## Overview and Traceability Matrix

| ID | User Story | Priority | Documented Status | Work Item | Implementation | Test Evidence |
| --- | --- | --- | --- | --- | --- | --- |
| US01 | Create tickets | High | Delivered | [PR #3](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/3) | `TicketController`, `TicketService` | `TicketControllerTest.shouldCreateTicket`, `TicketServiceTest.shouldCreateTicket` |
| US02 | Manage projects and members | High | Delivered | [Issue #59](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/59) · [PR #60](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/60) | `ProjectController`, `ProjectService` | `ProjectControllerTest.shouldCreateProject`, `shouldUpdateProject`, `shouldDeleteProject` |
| US03 | Restrict actions by user role | High | Delivered; further role-specific tests recommended | [Issue #68](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/68) · [PR #69](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/69) | `UserPermissionInterceptor`, `ProjectPermissionInterceptor` | `UserControllerTest.shouldReturnUnauthorizedWithoutSession` (authentication only; does not establish role coverage) |
| US04 | Manage ticket status and history | High | Delivered | [Issue #31](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/31) · [PR #56](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/56) | `TicketController`, `TicketActivityService` | `TicketControllerTest.shouldUpdateTicketStatus`, `TicketActivityServiceTest.shouldRecordActivityWhenValueChanges` |
| US05 | Comment on tickets | Medium | Delivered | [Issue #33](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/33) · [PR #43](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/43) | `TicketCommentController`, `TicketCommentService` | `TicketCommentServiceTest.shouldCreateComment`, `shouldListTicketsComments` |
| US06 | Manage user accounts | High | Delivered (create and delete) | [Issue #71](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/71) · [PR #70](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/70) | `UserController`, `UserService` | Controller tests exist; confirm tests specifically covering creation and deletion |
| US07 | View assigned tickets | Medium | Delivered | [Issue #75](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/75) · [PR #74](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/74) | `UserService.getAssignedTickets`, profile page | Confirm test coverage for retrieving assigned tickets |

The statuses above indicate that the corresponding features are present on `main`. They **do not** mean that every acceptance criterion below has a matching automated test.

## US01 — Ticket Creation

**User Story:** As an **authorized project user**, I want to **create tickets with a title, description, priority, and assignee**, so that **the team can track reported requests and issues**.

- **Priority:** High
- **Final status:** Delivered
- **Traceability:** [PR #3](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/3) · `TicketControllerTest.shouldCreateTicket` · `TicketServiceTest.shouldCreateTicket` · `TicketControllerTest.shouldReturnBadRequestWhenTitleIsBlank`

**Acceptance Criteria**

1. **Given** I am authenticated and have access to a project, **when** I submit valid ticket information, **then** the system creates the ticket and associates it with the selected project.
2. **Given** I am using the ticket form, **when** I submit a blank title, **then** validation rejects the request and no ticket is created.
3. **Given** I select a registered assignee, **when** the ticket is created, **then** that user is associated with the ticket.

## US02 — Project and Member Management

**User Story:** As an **administrator or project manager**, I want to **create and maintain projects and their members**, so that **work can be organized across different teams**.

- **Priority:** High
- **Final status:** Delivered
- **Traceability:** [Issue #59](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/59) · [PR #60](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/60) · `ProjectControllerTest.shouldCreateProject`, `shouldUpdateProject`, `shouldDeleteProject`, `shouldRejectProjectWithEmptyName`

**Acceptance Criteria**

1. **Given** I have permission to manage projects, **when** I provide a valid name, description, and members, **then** the project is persisted.
2. **Given** I have permission and the project exists, **when** I edit its details, **then** the updated information is saved.
3. **Given** I try to create a project without a name, **when** I submit the form, **then** validation rejects the request.

## US03 — Role-Based Access Control

**User Story:** As a **system administrator**, I want to **restrict management actions according to user roles**, so that **sensitive operations can be performed only by authorized users**.

- **Priority:** High
- **Final status:** Delivered, with additional role-specific test traceability needed
- **Traceability:** [Issue #68](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/68) · [PR #69](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/69) · `UserPermissionInterceptor` · `ProjectPermissionInterceptor`

**Acceptance Criteria**

1. **Given** a user with the `ADMIN` or `PROJECT_MANAGER` role, **when** that user requests a permitted management operation, **then** the system allows it.
2. **Given** a user without the required role, **when** that user attempts a restricted operation, **then** the system denies the action.
3. **Given** a user without an authenticated session, **when** that user requests a protected endpoint, **then** the request is rejected or redirected to authentication, depending on the route.

**Evidence gap:** Link exact automated test methods covering each role and the expected `403` response. An unauthenticated-session test is not evidence of role-based authorization.

## US04 — Ticket Status and Activity History

**User Story:** As a **project member**, I want to **update ticket status and view its activity history**, so that **I can follow the progress of reported issues**.

- **Priority:** High
- **Final status:** Delivered
- **Traceability:** [Issue #31](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/31) · [PR #56](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/56) · `TicketControllerTest.shouldUpdateTicketStatus` · `TicketActivityServiceTest.shouldRecordActivityWhenValueChanges`

**Acceptance Criteria**

1. **Given** I can access a ticket, **when** I change its status, **then** the new status is persisted.
2. **Given** a relevant change has occurred, **when** I view the activity history, **then** the event appears in the recorded timeline.
3. **Given** a ticket's status has not changed, **when** I submit the same status again, **then** the application does not record a fictitious change.

## US05 — Ticket Comments

**User Story:** As a **project member**, I want to **add and view comments on tickets**, so that **communication about the reported issues is recorded**.

- **Priority:** Medium
- **Final status:** Delivered
- **Traceability:** [Issue #33](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/33) · [PR #43](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/43) · `TicketCommentServiceTest.shouldCreateComment`, `shouldListTicketsComments`, `shouldAssociateCommentWithCorrectTicket`

**Acceptance Criteria**

1. **Given** an existing ticket, **when** I submit a valid comment, **then** the comment is persisted and linked to that ticket.
2. **Given** a ticket with comments, **when** I open its details, **then** its comments can be listed.
3. **Given** a nonexistent ticket, **when** I attempt to add a comment, **then** the operation is rejected.

## US06 — User Account Administration

**User Story:** As an **administrator or project manager**, I want to **create and delete user accounts**, so that **the registered team reflects the people participating in the system**.

- **Priority:** High
- **Final status:** Delivered for creation and deletion; account editing is outside this story's delivered scope
- **Traceability:** [Issue #71](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/71) · [PR #70](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/70) · `UserService` · `UserController`

**Acceptance Criteria**

1. **Given** I have user-management permissions, **when** I submit a valid name, email address, password, and role, **then** a new account is created.
2. **Given** an account with the specified email already exists, **when** I attempt to register the same email again, **then** creation is rejected.
3. **Given** I have permission and no associations prevent deletion, **when** I request deletion of a user account, **then** the account is removed.

**Evidence gap:** Add exact automated test methods covering account creation, duplicate emails, authorization, and deletion.

## US07 — My Assigned Tickets

**User Story:** As an **authenticated user**, I want to **view tickets assigned to me in my profile**, so that **I can keep track of my responsibilities**.

- **Priority:** Medium
- **Final status:** Delivered
- **Traceability:** [Issue #75](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/75) · [PR #74](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/74) · `UserService.getAssignedTickets` · profile page

**Acceptance Criteria**

1. **Given** I am authenticated, **when** I open my profile, **then** I can see tickets assigned to my account.
2. **Given** several tickets are assigned to me, **when** I view the list, **then** I can identify each ticket.
3. **Given** I am not authenticated, **when** I attempt to open the protected profile page, **then** access is restricted.

**Evidence gap:** Link exact tests covering user-specific ticket lists and access to assigned tickets.

## Keeping Traceability Up to Date

1. Keep each **issue** as a Kanban work item and link the PR implementing it.
2. Add the story ID (`US01`, `US02`, etc.) and a link to this file in the corresponding issue/card.
3. Map each acceptance criterion to the exact automated test method that verifies it; flag uncovered cases.
4. Update **priority**, **status**, and work-item references whenever new PRs change the behavior.

> **Important:** This document does not create or modify any GitHub Projects cards. Issue and PR links refer to existing records. Confirm that each issue is actually included in the team's Kanban board.
