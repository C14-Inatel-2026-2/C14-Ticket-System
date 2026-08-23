# Git Workflow

## For Contributors

The `main` branch is **protected**. Direct commits and pushes to `main` are not allowed.

To implement a new feature, fix a bug, update documentation, or change project configuration, create a new branch and open a **Pull Request** targeting `main`.

---

## Workflow

```bash
git pull origin main
```

Pull the latest changes from the `main` branch.

```bash
git checkout -b <type>/<name>
```

Create and switch to a new branch.

```bash
git add <files>
```

Add the changed, created, or deleted files to the staging area.

```bash
git commit -m "<description>"
```

Commit the changes using a clear and meaningful message.

```bash
git push -u origin <branch-name>
```

Push the new branch to GitHub.

After pushing the branch, open a **Pull Request** targeting `main`.

---

## Pull Request Requirements

Before a Pull Request can be merged:

* Jenkins CI must complete successfully.
* The required code review must be approved.
* The branch must satisfy the repository protection rules.

When the Pull Request resolves an Issue, reference it in the PR description:

```text
Closes #<issue-number>
```

Example:

```text
Closes #12
```

The Issue will be automatically closed after the Pull Request is merged.
To just reference an Issue, just use #<issue-number> without the keyword "Closes".
---

## Important

All Java files must contain Javadoc with an `@author` tag.

Example:

```java
/**
 * Handles ticket-related operations.
 *
 * @author Your Name
 */
public class TicketService {
}
```

The project build will fail if the required `@author` documentation is missing.

---

## Branch Naming Convention

Branches should follow this format:

```text
<type>/<short-description>
```

Prefer lowercase names and use hyphens between words.

### `feature/`

Used for new functionalities.

```text
feature/create-ticket
feature/user-login
```

### `fix/`

Used for bug fixes.

```text
fix/ticket-validation
fix/database-connection
```

### `chore/`

Used for configuration, maintenance, or infrastructure changes that do not directly add a new application feature.

```text
chore/database-config
chore/docker-setup
```

### `docs/`

Used exclusively for documentation changes.

```text
docs/contributing-guide
docs/database
```

### `test/`

Used for adding or updating tests.

```text
test/ticket-controller
test/ticket-service
```

---

## Example Branch Structure

```text
main
│
├── feature/create-ticket
├── feature/login
├── fix/ticket-validation
├── chore/database-config
├── docs/contributors
└── test/ticket-controller
```

