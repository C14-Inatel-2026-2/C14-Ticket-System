package com.ibm.tickets.browser;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

/**
 * Tests ticket editing form validation and API interactions.
 * @author Mariana Barude Pina
 */
class TicketEditBrowserTest {

    private static final String ORIGIN = "http://127.0.0.1:18788";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private final AtomicInteger puts = new AtomicInteger();
    private final AtomicReference<String> payload = new AtomicReference<>();

    
    @BeforeEach
    void openEditForm() throws Exception {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setChannel(System.getProperty("playwright.channel", "chromium"))
        );

        context = browser.newContext();

        String rawTemplate = Files.readString(Path.of("src/main/resources/templates/ticket-edit.html"));
        final String template = rawTemplate.replace("/*[[${ticket.id}]]*/ 0", "2").replace("/*[[${ticket.project}]]*/\"\"", "\"Ticket System\"");
        String projectNavigation = Files.readString(Path.of("src/main/resources/static/js/project-navigation.js"));

        context.route(ORIGIN + "/**", route -> {
            String url = route.request().url();

            if (url.endsWith("/tickets/2/edit")) {
                route.fulfill(new Route.FulfillOptions()
                        .setContentType("text/html")
                        .setBody(template));

            } else if (url.endsWith("/js/project-navigation.js")) {
                route.fulfill(new Route.FulfillOptions()
                        .setContentType("application/javascript")
                        .setBody(projectNavigation));

            } else {
                route.fulfill(new Route.FulfillOptions()
                        .setContentType("text/html")
                        .setBody("<p>Lista de tickets</p>"));
            }
        });

        page = context.newPage();

        page.onRequest(request -> {
            if (request.method().equals("PUT")
                    && request.url().endsWith("/api/tickets/2")) {
                puts.incrementAndGet();
                payload.set(request.postData());
            }
        });

        page.route("**/api/tickets/2", route ->
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("{\"id\":2}")
                )
        );

        page.navigate(ORIGIN + "/project/3/tickets/2/edit");

        page.locator("#title").fill("Teste de edição");
        page.locator("#owner").fill("Mariana");
        page.locator("#priority").selectOption("HIGH");
        page.locator("#description").fill("Descrição original do ticket");
    }
    
    @AfterEach
    void closeBrowser(TestInfo info) {
        try {
            if (page != null && !page.isClosed()) {
                Path image = Path.of(
                        "target/browser-artifacts/"
                        + info.getTestMethod().orElseThrow().getName()
                        + "-" + UUID.randomUUID() + ".png"
                );

                try {
                    Files.createDirectories(image.getParent());
                    page.screenshot(new Page.ScreenshotOptions()
                            .setPath(image)
                            .setFullPage(true));
                } catch (Exception ignored) {
                    // A screenshot must not mask the original failure.
                }
            }
        } finally {
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwright != null) {
                playwright.close();
            }
        }
    }

    private void fillValid() {
        page.locator("#title").fill("Teste de edição");
        page.locator("#owner").fill("Mariana");
        page.locator("#priority").selectOption("HIGH");
        page.locator("#description").fill("Descrição original do ticket");
    }

    private void respond(int status, String contentType, String body) {
        page.unroute("**/api/tickets/2");

        page.route("**/api/tickets/2", route ->
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(status)
                        .setContentType(contentType)
                        .setBody(body)
                )
        );
    }

    private void submit() {
        page.locator("button[type=submit]").click();
    }

    
    static Stream<Arguments> textLimits() {
        return Stream.of(
                Arguments.of("title", 150),
                Arguments.of("owner", 100)
        );
    }

    private void assignRawValue(String field, String value) {
        page.evaluate("""
                ({field, value}) => {
                    document.getElementById(field).value = value;
                }
                """, Map.of("field", field, "value", value));
    }

    @ParameterizedTest
    @MethodSource("textLimits")
    void rejectsValueAboveCharacterLimit(String field, int limit) {
        fillValid();
        assignRawValue(field, "x".repeat(limit + 1));

        submit();

        assertThat(page.locator("#" + field + "-error")).isVisible();
        assertThat(page.locator("#" + field)).hasAttribute("aria-invalid", "true");

        assertEquals(0, puts.get());
    }

    @ParameterizedTest
    @MethodSource("textLimits")
    void acceptsValueAtCharacterLimit(String field, int limit) {
        fillValid();

        String value = "x".repeat(limit);
        assignRawValue(field, value);

        submit();

        assertEquals(1, puts.get());
        assertTrue(payload.get().contains(value));
        assertThat(page).hasURL(ORIGIN + "/project/3/tickets");
    }


    @Test
    void rejectsAllEmptyRequiredFieldsWithoutUpdating() {
        page.locator("#title").fill("");
        page.locator("#owner").fill("");
        page.locator("#priority").selectOption("");
        page.locator("#description").fill("");

        submit();

        for (String field : new String[]{
                "title", "owner", "priority", "description"
        }) {
            assertThat(page.locator("#" + field + "-error")).isVisible();
            assertThat(page.locator("#" + field)).hasAttribute("aria-invalid", "true");
        }

        assertThat(page.locator("#title")).isFocused();
        assertThat(page.locator("#form-feedback")).containsText("Revise os campos indicados");

        assertEquals(0, puts.get());
    }

    @Test
    void rejectsMissingTitleWithoutUpdating() {
        page.locator("#title").fill("");

        submit();

        assertThat(page.locator("#title-error")).containsText("Informe o título.");
        assertThat(page.locator("#title")).isFocused();
        assertThat(page.locator("#title")).hasAttribute("aria-invalid", "true");

        assertEquals(0, puts.get());
    }

    @Test
    void rejectsMissingOwnerWithoutUpdating() {
        page.locator("#owner").fill("");

        submit();

        assertThat(page.locator("#owner-error")).containsText("Informe o responsável.");
        assertThat(page.locator("#owner")).isFocused();
        assertThat(page.locator("#owner")).hasAttribute("aria-invalid", "true");

        assertEquals(0, puts.get());
    }

    @Test
    void rejectsMissingPriorityWithoutUpdating() {
        page.locator("#priority").selectOption("");

        submit();

        assertThat(page.locator("#priority-error")).containsText("Selecione a criticidade.");
        assertThat(page.locator("#priority")).isFocused();

        assertEquals(0, puts.get());
    }

    @Test
    void rejectsMissingDescriptionWithoutUpdating() {
        page.locator("#description").fill("");

        submit();

        assertThat(page.locator("#description-error")).containsText("Informe a descrição.");
        assertThat(page.locator("#description")).isFocused();

        assertEquals(0, puts.get());
    }

    
    @Test
    void mapsApiValidationErrorsAndPreservesValues() {
        fillValid();

        respond(400, "application/problem+json", """
                {
                    "status": 400,
                    "title": "Validation error",
                    "errors": {
                        "assignee": "Assignee is required",
                        "title": "Title is required"
                    }
                }""");

        submit();

        assertThat(page.locator("#owner-error")).containsText("Informe o responsável.");
        assertThat(page.locator("#title-error")).containsText("Informe o título.");
        assertThat(page.locator("#owner")).hasValue("Mariana");
        assertThat(page.locator("#description")).hasValue("Descrição original do ticket");

        assertEquals(1, puts.get());

        page.locator("#owner").fill("Outra pessoa");

        assertThat(page.locator("#owner-error")).isHidden();

        respond(200, "application/json", "{\"id\":2}");

        submit();

        assertThat(page).hasURL(ORIGIN + "/project/3/tickets");
        assertEquals(2, puts.get());
    }

    static Stream<Arguments> errorResponses() {
        return Stream.of(
                Arguments.of(401, "text/plain", ""),
                Arguments.of(404, "text/plain", ""),
                Arguments.of(500, "text/html", "<h1>Internal Server Error</h1>"),
                Arguments.of(400, "application/json", "{"),
                Arguments.of(400, "application/json", "{\"errors\":{}}")
        );
    }

    @ParameterizedTest
    @MethodSource("errorResponses")
    void handlesApiErrorsWithoutLosingFormValues(int status, String contentType, String body) {

        fillValid();

        respond(status, contentType, body);

        submit();

        assertThat(page.locator("#form-feedback")).isVisible();
        assertThat(page.locator("#title")).hasValue("Teste de edição");
        assertThat(page.locator("#owner")).hasValue("Mariana");
        assertThat(page.locator("#description")).hasValue("Descrição original do ticket");
        assertThat(page).hasURL(ORIGIN + "/project/3/tickets/2/edit");

        assertEquals(1, puts.get());

        if (status == 401) {
            assertThat(page.locator("#form-feedback")).containsText("sessão");
        }

        if (status == 404) {
            assertThat(page.locator("#form-feedback")).containsText("não foi encontrado");
        }
    }

    @Test
    void reportsNetworkFailureWithoutLosingFormValues() {
        fillValid();

        page.unroute("**/api/tickets/2");

        page.route("**/api/tickets/2", route ->
                route.abort()
        );

        submit();

        assertThat(page.locator("#form-feedback")).containsText("Seus dados foram mantidos");
        assertThat(page.locator("#title")).hasValue("Teste de edição");
        assertThat(page.locator("#owner")).hasValue("Mariana");
        assertThat(page).hasURL(ORIGIN + "/project/3/tickets/2/edit");

        assertEquals(1, puts.get());
    }
    @Test
    void sendsCompleteTicketDataWithOriginalProject() {
        fillValid();

        page.waitForRequest(
                request -> request.method().equals("PUT")
                        && request.url().endsWith("/api/tickets/2"),
                () -> submit()
        );

        assertEquals(1, puts.get());

        assertTrue(payload.get().contains("\"title\":\"Teste de edição\""));
        assertTrue(payload.get().contains("\"project\":\"Ticket System\""));
        assertTrue(payload.get().contains("\"assignee\":\"Mariana\""));
        assertTrue(payload.get().contains("\"priority\":\"HIGH\""));
        assertTrue(payload.get().contains("\"description\":\"Descrição original do ticket\""));

        assertThat(page).hasURL(ORIGIN + "/project/3/tickets");
    }

    @Test
    void clearsFieldErrorsWhenValuesAreCorrected() {
        page.locator("#title").fill("");
        page.locator("#owner").fill("");
        page.locator("#priority").selectOption("");
        page.locator("#description").fill("");

        submit();

        for (String field : new String[]{
                "title", "owner", "priority", "description"
        }) {
            assertThat(page.locator("#" + field + "-error")).isVisible();
        }

        page.locator("#title").fill("Título corrigido");
        page.locator("#owner").fill("Mariana");
        page.locator("#priority").selectOption("HIGH");
        page.locator("#description").fill("Descrição corrigida");

        for (String field : new String[]{
                "title", "owner", "priority", "description"
        }) {
            assertThat(page.locator("#" + field + "-error")).isHidden();
            assertThat(page.locator("#" + field)).not().hasAttribute("aria-invalid", "true");
        }

        assertEquals(0, puts.get());
    }

}
