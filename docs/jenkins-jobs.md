# Jenkins Pipeline Jobs and Stages — C14 Ticket System 

This document explains each stage defined in the [`Jenkinsfile`](../Jenkinsfile) on `main`, including its purpose, execution conditions, and expected outputs.

## Pipeline Overview

```text
GitHub (Pull Request / main)
  |
  +-- Checkout
  +-- Compile
  +-- Test + JaCoCo
  +-- Package
  +-- Build and Push Image [main only]
  +-- Deploy to Railway - Staging    [main, DEPLOY_ENV=staging|both]
  +-- Deploy to Railway - Production [main, DEPLOY_ENV=production|both]
```

The `DEPLOY_ENV` parameter offers three options: `staging`, `production`, and `both`. In the reviewed configuration, `staging` is the first choice, so it is the default for parameterized builds when no other option is explicitly selected.

## Stage Ownership Matrix

| Stage | Purpose | Runs for PRs? | Responsible Team Member | Implementation Commit / PR |
| --- | --- | --- | --- | --- |
| Checkout | Retrieve source code from the repository | Yes | **Rodrigo Costa** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/1** |
| Compile | Compile Java source code using the Maven Wrapper | Yes | **Rodrigo Costa** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/1** |
| Test | Execute automated tests and publish reports | Yes | **Gabriel Guimarães** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/24** |
| Package | Build the application artifact without repeating tests | Yes | **Rodrigo Costa** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/1** |
| Build and Push Image | Build and publish the Docker image to GHCR | No; `main` only | **Mariana Barude** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/26** |
| Deploy to Railway - Staging | Request a staging redeployment | No; selected on `main` | **Rodrigo Costa** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/80** |
| Deploy to Railway - Production | Request a production redeployment | No; selected on `main` | **Rodrigo Costa** | **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/80** |

## 1. Checkout

**Main command:** `checkout scm`.

Jenkins checks out the revision that triggered the build, whether it came from a branch or pull request. Subsequent stages operate on the files associated with that revision.

**Success criteria:** Checkout completes and source files are available in the Jenkins agent's workspace.

**Responsible team member:** **Rodrigo Costa**. **Authorship evidence:** **(https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/1)**.

## 2. Compile

**Main commands:** `chmod +x mvnw` and `./mvnw clean compile`.

The Maven Wrapper uses the project's Maven configuration. This stage verifies that the Java sources compile and that the required dependencies can be resolved. The `clean` phase removes previously generated build output before compilation.

**Success criteria:** Compilation finishes with a zero exit code.

**Responsible team member:** **Rodrigo Costa**. **Authorship evidence:** **(https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/1)**.

## 3. Test and Coverage Reports

**Main command:** `./mvnw test jacoco:report`.

This stage executes the project's default Maven test lifecycle. A `post { always { ... } }` block collects reports even if test execution fails:

- `junit('target/surefire-reports/*.xml')`: publishes Surefire test results in Jenkins.
- `archiveArtifacts('target/site/jacoco/**')`: stores the coverage report as a build artifact.
- `publishHTML(...)`: makes the JaCoCo HTML report available from the Jenkins interface.

**Scope limitation:** The default Surefire configuration excludes tests under `browser/` and `e2e/`. Those tests are enabled separately by the Maven `browser-tests` profile. Therefore, this stage does not execute the entire browser/E2E suite.

**Success criteria:** Executed tests pass and configured reports are published.

**Responsible team member:** **Gabriel Guimarães**. **Authorship evidence:** **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/24**.

## 4. Package

**Main command:** `./mvnw package -DskipTests`.

This stage produces the packaged application artifact for the Docker build. Tests are skipped **for this second Maven invocation** because they have already run in the preceding Test stage. The `Test` stage must still pass for the pipeline to continue normally.

**Success criteria:** Packaging completes and the application artifact is generated.

**Responsible team member:** **Rodrigo Costa**. **Authorship evidence:** **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/1**.

## 5. Build and Push Image

**Condition:** `when { branch 'main' }`.

Jenkins authenticates to GitHub Container Registry (GHCR) using the protected `ghcr-credentials` credential. It then builds and publishes the Docker image with two tags:

```text
ghcr.io/c14-inatel-2026-2/c14-ticket-system:${BUILD_NUMBER}
ghcr.io/c14-inatel-2026-2/c14-ticket-system:latest
```

The `BUILD_NUMBER` tag identifies the image published by a particular Jenkins build. The `latest` tag is mutable and can point to newer images over time.

The pipeline logs out from the Docker registry at the end of this stage instead of keeping the registry session open.

**Success criteria:** Both image tags are successfully pushed to GHCR.

**Responsible team member:** **Mariana Barude**. **Authorship evidence:** **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/26**.

## 6. Deploy to Railway - Staging

**Condition:** The branch is `main`, and `DEPLOY_ENV` is `staging` or `both`.

Jenkins injects the `railway-staging-token` credential as `RAILWAY_TOKEN` into a temporary Node.js container that runs the Railway CLI:

```sh
npx -y @railway/cli@latest redeploy --service c14-tickets-api --yes
```

This stage requests a **redeployment of the existing deployment** in the environment scoped by the staging token. The command does not explicitly provide the newly published Docker image tag based on `BUILD_NUMBER`.

**Important limitation:** A successful CLI invocation confirms that the redeployment request was accepted. On its own, it does not prove that the updated `latest` image was used or that the application passed its health checks.

**Success criteria:** Railway accepts the command; the deployment status and running application version are then checked in the staging environment.

**Responsible team member:** **Rodrigo Costa**. **Authorship evidence:** **https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/80**.

## 7. Deploy to Railway - Production

**Condition:** The branch is `main`, and `DEPLOY_ENV` is `production` or `both`.

This stage works like the staging deployment but uses `railway-token`, which is scoped to production. Separate credentials keep each environment's deployment access independent.

When `DEPLOY_ENV=both`, the staging stage runs before production. **There is currently no explicit gate that waits for a successful staging health check before production starts.**

**Success criteria:** Railway accepts the command; the production deployment and application status are verified separately.

**Responsible team member:** **Rodrigo Costa**. **Authorship evidence:** **(https://github.com/C14-Inatel-2026-2/C14-Ticket-System/pull/80)**.

## Execution by Environment

| Build Type | Checkout / Compile / Test / Package | Build and Push | Staging Deploy | Production Deploy |
| --- | --- | --- | --- | --- |
| Pull request | Yes | No | No | No |
| `main` + `staging` | Yes | Yes | Yes | No |
| `main` + `production` | Yes | Yes | No | Yes |
| `main` + `both` | Yes | Yes | Yes | Yes |

