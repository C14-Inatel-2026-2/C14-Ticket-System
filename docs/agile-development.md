# Agile Development Methodology — C14 Ticket System 

## 1. Overview

The C14 Ticket System was developed collaboratively using an **agile, Kanban-oriented workflow**. The team organized work using **GitHub Projects (Kanban board)**, created GitHub issues to describe tasks and improvements, and implemented changes incrementally through branches and pull requests (PRs).

Rather than assigning the entire application to a single contributor, team members were primarily responsible for different parts of the project, while code integration and review remained collaborative. The repository provides evidence of incremental implementation across the backend, frontend, persistence, automated tests, and CI/CD pipeline.

This document describes the workflow supported by repository evidence. It does **not** claim that the team held formal Scrum ceremonies or fixed-length sprints, since those details are not established by the available project records.

## 2. Why a Kanban-Oriented Approach?

Kanban was appropriate for the team's incremental work: tasks could be created as issues, prioritized as requirements emerged, implemented independently, reviewed, and completed without waiting for a fixed sprint boundary.

The board provided a shared view of work. GitHub issues described the changes; branches and PRs supplied the technical evidence of implementation. GitHub was therefore used for both **work organization** and **source-code collaboration**.

**Project and tracking links**

- [GitHub Projects — C14-Inatel-2026-2 organization](https://github.com/orgs/C14-Inatel-2026-2/projects) — project directory containing the Kanban board
- [C14 Ticket System — GitHub Issues](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues)
- [C14 Ticket System — Pull Requests](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pulls)
- [User Stories and Traceability](user-stories.md)

## 3. Work Organization with GitHub Projects

The practical process was centered on **issues, feature branches, pull requests, and reviews**:

1. **Identify a requirement or improvement.** Create a GitHub issue describing a feature, bug, test, documentation change, or infrastructure task.
2. **Organize the issue on the Kanban board.** Track its current state and priority. For documentation purposes, a suitable flow is *To Do → In Progress → In Review → Done*; these labels are a proposed representation, not a verified transcription of the team's board columns.
3. **Assign responsibility.** A team member takes primary ownership of implementation. Collaboration, review, and integration can involve other members.
4. **Implement in a separate branch.** Use the conventions described in [`docs/contributing.md`](contributing.md), such as `feature/`, `fix/`, `test/`, `docs/`, or `chore/`.
5. **Open a pull request.** Describe the change, refer to the related issue, and explain how to test it. The repository includes a shared PR template.
6. **Review and validate.** A teammate reviews the PR while Jenkins performs compilation, automated tests, and report publication.
7. **Merge and close the work item.** After the required checks and reviews, the PR is merged into `main`; the corresponding issue may then be closed and its Kanban card updated.

### Traceable examples from the repository

| Work item | GitHub issue | Implementation PR | What it demonstrates |
| --- | --- | --- | --- |
| Project CRUD | [#59](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/59) | [#60](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/60) | A functional requirement implemented through a feature PR |
| Role-based restrictions | [#68](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/68) | [#69](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/69) | Access-control functionality developed separately |
| Ticket comments | [#33](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/33) | [#43](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/43) | Frontend and backend functionality integrated via review |
| Assigned tickets in user profile | [#75](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/75) | [#74](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/74) | Iterative enhancement of an existing page |
| Railway deployment | [#62](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/issues/62) | [#65](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/65) | Infrastructure and deployment as managed tasks |

## 4. Team Responsibilities and Collaboration

Tasks were divided so that different contributors could implement different parts of the application. The following areas are supported by PR and commit history; **formal roles and final responsibility assignments should be confirmed by the team**.

| Contributor | Examples of visible contributions | Example PRs | Formal team role |
| --- | --- | --- | --- |
| Rodrigo Fraga da Costa | Project and user management, role permissions, Jenkins/Railway deployment, ticket activity integration | [#60](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/60), [#69](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/69), [#78](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/78) | **Leader, BackEnd Developer, DevOps** |
| Mariana Barude Pina | Ticket interface, project-ticket integration, validation and review, refactoring | [#61](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/61), [#63](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/63), [#76 review](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/76) | **FrontEnd Developer and QA** |
| Gabriel Guimaraes | Ticket domain and persistence, service/controller tests, refactoring and coverage reporting | [#3](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/3), [#24](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/24), [#54](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/54) | **BackendEnd Developer - Focused on Database and QA** |

These are **areas of emphasis**, not exclusive ownership: all contributors were expected to understand and review the integrated system. If additional team members participated, add their verified responsibilities and evidence here.

## 5. Development Cadence and Communication

The repository shows **incremental delivery**: multiple issues and PRs were completed over the semester, with code review and CI used to validate integrations. Work was not limited to a single, final merge.

Record the team's actual communication practices before submission:

| Topic | Recorded approach / information to complete |
| --- | --- |
| Iteration style | Kanban-oriented, continuous task flow; no fixed sprint length established from repository evidence |
| Planning | GitHub issues and GitHub Projects board |
| Collaboration | Feature branches, pull requests, and code reviews |
| Automated feedback | Jenkins pipeline: compilation, tests, JaCoCo reports, and environment deployment |


Do not describe meetings, sprint planning, daily stand-ups, or retrospectives as historical facts unless the team actually performed them.

## 6. Definition of Ready and Definition of Done

The items below are a **proposed formalization**, based on existing issue, PR, and CI practices. The team should confirm which rules were already followed and which are process improvements introduced for NP2 documentation.

### Suggested Definition of Ready (DoR)

A work item is ready when it has a comprehensible problem statement, expected result or acceptance criteria, an agreed priority, known dependencies, and an assigned primary contributor.

### Suggested Definition of Done (DoD)

A change is complete when the implementation meets its acceptance criteria, relevant tests are updated and passing, Jenkins validation succeeds, the PR receives the required review, documentation is updated where necessary, and the change is integrated into `main`.

The repository's [Contribution Guidelines](contributing.md) already describe feature branches, PR review, checks, and protected `main` workflow.

## 7. Review, Blockers, and Adaptation

### Example: project access versus ticket assignment

In [PR #76](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/76), a reviewer identified a potential conflict between project-access rules and the *My Tickets* feature: a person assigned to a ticket might not belong to the project, preventing them from opening it. The implementation was adjusted so the ticket form displayed eligible project members, and the review was subsequently approved.

This is a concrete example of agile collaboration: an issue was uncovered during review, discussed, and addressed before integration. The review also suggested a possible future improvement to enforce the membership restriction in the backend.

### Example: staging deployment and pipeline adjustments

[PR #78](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/78) introduced the staging environment and seed data. [PR #80](https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/80) adjusted the Railway deployment flow after the team investigated token and CLI behavior. These changes show how implementation feedback led to improvements in the delivery process.

### Lessons learned

The examples above support three lessons:

- Reviewing changes across features helps detect integration conflicts that isolated implementation may miss.
- Small, traceable PRs make it easier to identify why a change was introduced and what was validated.
- CI/CD infrastructure is also software: authentication, deployment commands, and environment isolation need explicit verification.

**Team reflection to complete:** What most often blocked work, how priorities changed, and what the group would do differently in a future project.
