package com.ibm.tickets.browser;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

class TicketStatusHistoryBrowserTest {

    private static final String ORIGIN = "http://127.0.0.1:18788";
    private static final Long TICKET_ID = 7L;

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;
    private String lastStatusPayload;
    private int statusPatchCount;
    private String lastStatusUrl;

    @BeforeEach
    void openTicketDetails() throws Exception {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setChannel(System.getProperty(
                                "playwright.channel", "chromium"))
        );

        context = browser.newContext();
        page = context.newPage();
        page.onRequest(request -> {
            if (request.method().equals("PATCH")
                && request.url().contains("/api/tickets/" + TICKET_ID + "/status?projectId=3")) {
                statusPatchCount++;
                lastStatusUrl = request.url();
                lastStatusPayload = request.postData();
            }
        });

        String rawTemplate = Files.readString(
            Path.of("src/main/resources/templates/ticket-details.html")
        );

        String template = rawTemplate
            .replace(
                "th:classappend=\"${ticket.status.toString() == 'OPEN' ? ' open' : (ticket.status.toString() == 'IN_PROGRESS' ? ' progress' : (ticket.status.toString() == 'RESOLVED' ? ' resolved' : (ticket.status.toString() == 'CLOSED' ? ' closed' : '')))}\"",
                "th:classappend=\"\""
        )
            .replace(
                "th:text=\"${ticket.status.toString() == 'OPEN' ? 'Aberto' : (ticket.status.toString() == 'IN_PROGRESS' ? 'Em andamento' : (ticket.status.toString() == 'RESOLVED' ? 'Resolvido' : (ticket.status.toString() == 'CLOSED' ? 'Encerrado' : ticket.status)))}\"",
                ""
        )   .replace(
                "th:attr=\"data-current-status=${ticket.status}\"",
                "data-current-status=\"OPEN\""
        );

        String commentsScript = Files.readString(
            Path.of("src/main/resources/static/js/ticket-comments.js")
        );

        String navigationScript = Files.readString(
            Path.of("src/main/resources/static/js/project-navigation.js")
        );

        context.route(ORIGIN + "/**", route -> {
            String url = route.request().url();

            if (url.endsWith("/js/project-navigation.js")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/javascript")
                        .setBody(navigationScript));
            } else if (url.endsWith("/js/ticket-comments.js")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/javascript")
                        .setBody(commentsScript));
            } else if (url.endsWith("/api/tickets/7/activities")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("[]"));
            } else if (url.endsWith("/api/tickets/7")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("{}"));
            } else if (url.contains("/api/tickets/7/status?projectId=3")) {
                route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("""
                    {
                        "id": 7,
                        "status": "IN_PROGRESS"
                    }
                """));
            } else {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("text/html")
                        .setBody(template));
            }
        });

        page.navigate(ORIGIN + "/project/3/tickets/" + TICKET_ID);

        page.locator(".details-badges .status").evaluate("""
            element => {
                element.classList.add("open");
                element.textContent = "Aberto";
            }""");
        page.locator("#status-select").evaluate("""
            element => {
                element.innerHTML = `
                    <option value="OPEN" selected>Aberto</option>
                    <option value="IN_PROGRESS">Em andamento</option>
                    <option value="RESOLVED">Resolvido</option>
                    <option value="CLOSED">Encerrado</option>
                `;
            }""");
    }

    @Test
    void displaysTicketStatusAndActivitySection() {
        assertThat(page.locator("#status-select")).isVisible();

        assertThat(page.locator(".details-badges .status"))
                .isVisible();

        assertThat(page.locator("#ticket-timeline"))
                .containsText("Nenhuma atividade registrada.");
    }
    @Test
    void updatesTicketStatusSuccessfully() {
        page.locator("#status-select").selectOption("IN_PROGRESS");

        assertThat(page.locator(".details-badges .status")).hasText("Em andamento");

        assertThat(page.locator(".details-badges .status")).hasClass("status progress");

        assertTrue(
            lastStatusUrl != null 
            && lastStatusUrl.contains("projectId=3"), "A requisição PATCH deveria conter projectId=3");
        assertEquals(1, statusPatchCount);
        assertEquals(
            "{\"status\":\"IN_PROGRESS\"}",
            lastStatusPayload
        );

        assertThat(page.locator("#status-feedback")).containsText("Status atualizado com sucesso!");
    }

    private void mockStatusFailure() {
        page.route("**/api/tickets/7/status*", route -> {
            String url = route.request().url();

            if (url.contains("projectId=3")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("""
                            {"error": "Internal Server Error"}""")
                );
            } else {
                route.resume();
            }
        });
    }

    @Test
    void preservesPreviousStatusWhenApiFails() {
        mockStatusFailure();

        page.locator("#status-select").selectOption("RESOLVED");

        assertThat(page.locator("#status-select")).hasValue("OPEN");

        assertThat(page.locator(".details-badges .status")).hasText("Aberto");

        assertThat(page.locator("#status-feedback")).containsText("Não foi possível atualizar o status.");

        assertEquals(1, statusPatchCount);
    }

    @AfterEach
    void closeBrowser() {
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
