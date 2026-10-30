package com.ibm.tickets.browser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Exercises the real form script with controlled API responses, not a full E2E server.
 * @author C14 Team
 */
class NewTicketBrowserTest {
    private static final String ORIGIN = "http://127.0.0.1:18787";
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private final AtomicInteger posts = new AtomicInteger();
    private final AtomicReference<String> payload = new AtomicReference<>();

    @BeforeEach
    void openForm() throws Exception {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setChannel(System.getProperty("playwright.channel", "chromium")));
        context = browser.newContext();
        String template = Files.readString(Path.of("src/main/resources/templates/new-ticket.html"));
        String projectNavigation = Files.readString(Path.of("src/main/resources/static/js/project-navigation.js"));
        context.route(ORIGIN + "/**", route -> {
            String url = route.request().url();
            if (url.endsWith("/tickets/new")) {
                route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(template));
            } else if (url.endsWith("api/projects")){
                route.fulfill(new Route.FulfillOptions().setContentType("application/json").setBody("""
                    [{"id": "3", "name": "Ticket System"}]
                    """));
            } else if (url.endsWith("/js/project-navigation.js")) {
                route.fulfill(new Route.FulfillOptions().setContentType("application/javascript").setBody(projectNavigation));
            }else {
                route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody("<p>Lista</p>"));
            }
        });
        page = context.newPage();
        page.onRequest(request -> {
            if (request.method().equals("POST") && request.url().endsWith("/api/tickets")) {
                posts.incrementAndGet();
                payload.set(request.postData());
            }
        });
        respond(500, "text/plain", "unavailable");
        page.navigate(ORIGIN + "/project/3/tickets/new");
    }

    @AfterEach
    void closeBrowser(TestInfo info) {
        try {
            if (page != null && !page.isClosed()) {
                Path image = Path.of("target/browser-artifacts/" + info.getTestMethod().orElseThrow().getName() + "-" + UUID.randomUUID() + ".png");
                try {
                    Files.createDirectories(image.getParent());
                    page.screenshot(new Page.ScreenshotOptions().setPath(image).setFullPage(true));
                } catch (Exception ignored) { /* A screenshot must not mask the original failure. */ }
            }
        } finally {
            if (context != null) context.close();
            if (browser != null) browser.close();
            if (playwright != null) playwright.close();
        }
    }

    private void fillValid() {
        page.locator("#title").fill("Impressora sem papel");
        page.locator("#owner").fill("Pessoa de teste");
        page.locator("#priority").selectOption("HIGH");
        page.locator("#description").fill("Descrição preservada");
    }

    private void respond(int status, String contentType, String body) {
        page.unroute("**/api/tickets");
        page.route("**/api/tickets", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(status).setContentType(contentType).setBody(body)));
    }

    private void submit() { page.locator("button[type=submit]").click(); }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejectsMissingOwnerWithoutPosting(String value) {
        fillValid();
        page.locator("#owner").fill(value);
        submit();
        assertThat(page.locator("#owner-error")).isVisible();
        assertThat(page.locator("#owner")).isFocused();
        assertThat(page.locator("#owner")).hasAttribute("aria-invalid", "true");
        assertThat(page.locator("#owner")).hasAttribute("aria-describedby", "owner-hint owner-error");
        assertEquals(0, posts.get());
    }

    @Test
    void enterReportsAllRequiredFields() {
        page.locator("#title").press("Enter");
        for (String field : new String[]{"title", "owner", "priority", "description"}) {
            assertThat(page.locator("#" + field + "-error")).isVisible();
        }
        assertThat(page.locator("#title")).isFocused();
        assertThat(page.locator("#form-feedback")).hasAttribute("role", "alert");
        assertEquals(0, posts.get());
    }

    static Stream<Arguments> textLimits() {
        return Stream.of(Arguments.of("title", 150), Arguments.of("owner", 100));
    }

    private void assignRawValue(String field, String value) {
        page.evaluate("""
                ({field, value}) => {
                    const input = document.getElementById(field);
                    if (input.tagName === 'SELECT') input.add(new Option(value, value));
                    input.value = value;
                }
                """, Map.of("field", field, "value", value));
    }

    @ParameterizedTest
    @MethodSource("textLimits")
    void rejectsInjectedValueAboveLimit(String field, int limit) {
        fillValid();
        assignRawValue(field, "x".repeat(limit + 1));
        submit();
        assertThat(page.locator("#" + field + "-error")).isVisible();
        assertEquals(0, posts.get());
    }

    @ParameterizedTest
    @MethodSource("textLimits")
    void sendsBoundaryValueUnchanged(String field, int limit) {
        fillValid();
        String value = "x".repeat(limit - 1) + " ";
        assignRawValue(field, value);
        submit();
        assertThat(page.locator("#form-feedback")).isVisible();
        assertEquals(1, posts.get());
        assertTrue(payload.get().contains(value));
        assertThat(page.locator("#" + field)).hasValue(value);
    }

    @Test
    void mapsApiErrorsAndClearsThemBeforeSuccessfulRetry() {
        fillValid();
        respond(400, "application/problem+json", """
                {"status":400,"title":"Validation error","errors":{
                  "assignee":"Assignee is required","title":"Title is required"}}
                """);
        submit();
        assertThat(page.locator("#owner-error")).containsText("Informe o responsável");
        assertThat(page.locator("#title-error")).containsText("Informe o título");
        assertThat(page.locator("#owner")).hasValue("Pessoa de teste");
        assertThat(page.locator("#description")).hasValue("Descrição preservada");
        page.locator("#owner").fill("Outra pessoa");
        assertThat(page.locator("#owner-error")).isHidden();
        respond(201, "application/json", "{\"id\":1}");
        submit();
        assertThat(page).hasURL(ORIGIN + "/project/3/tickets");
        assertEquals(2, posts.get());
    }

    static Stream<Arguments> unusableResponses() {
        return Stream.of(
                Arguments.of(401, "text/plain", ""),
                Arguments.of(500, "text/html", "<h1>internal failure</h1>"),
                Arguments.of(400, "application/json", "{"),
                Arguments.of(400, "application/json", "{\"errors\":{}}"),
                Arguments.of(400, "application/json", "{\"errors\":{\"unknown\":\"detail\"}}"),
                Arguments.of(400, "application/json", "{\"errors\":{\"assignee\":[]}}"),
                Arguments.of(400, "application/json", "{\"errors\":{\"title\":\"<img src=x onerror=alert(1)>\"}}"));
    }

    @ParameterizedTest
    @MethodSource("unusableResponses")
    void handlesUnusableResponsesWithoutLosingValues(int status, String type, String body) {
        fillValid();
        respond(status, type, body);
        submit();
        assertThat(page.locator("#form-feedback")).isVisible();
        assertThat(page.locator("#title")).hasValue("Impressora sem papel");
        assertThat(page.locator("#owner")).hasValue("Pessoa de teste");
        assertThat(page.locator("#description")).hasValue("Descrição preservada");
        assertThat(page).hasURL(ORIGIN + "/project/3/tickets/new");
        assertEquals(0, page.locator("#ticket-form img").count());
        assertEquals(1, posts.get());
        if (status == 401) assertThat(page.locator("#form-feedback")).containsText("sessão");
    }

    @Test
    void reportsUncertainNetworkOutcomeWithoutRetry() {
        fillValid();
        page.unroute("**/api/tickets");
        page.route("**/api/tickets", route -> route.abort("failed"));
        submit();
        assertThat(page.locator("#form-feedback")).containsText("Seus dados foram mantidos");
        assertThat(page.locator("#owner")).hasValue("Pessoa de teste");
        assertThat(page).hasURL(ORIGIN + "/project/3/tickets/new");
        assertEquals(1, posts.get());
    }

    @Test
    void includesCurrentProjectInTicketPayload() {
        fillValid();
        page.waitForRequest(
            request -> request.method().equals("POST")
                    && request.url().endsWith("/api/tickets"),
            () -> submit()
        );

        assertEquals(1, posts.get());
        assertTrue(payload.get().contains("\"project\":\"Ticket System\""));
    }
}
