package com.ibm.tickets.browser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TicketListSecurityBrowserTest {
    private static final String ORIGIN = "http://127.0.0.1:18788";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private final AtomicInteger alertCount = new AtomicInteger();
    private String lastApiUrl;

    @BeforeEach
    void openTicketList() throws Exception {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setChannel(System.getProperty("playwright.channel", "chromium"))
        );
        context = browser.newContext();
        page = context.newPage();

        page.addInitScript("""
                window.__xssExecuted = 0;
                window.alert = () => {window.__xssExecuted++;};
                """);

        String template = Files.readString(
                Path.of("src/main/resources/templates/home.html")
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
            } else if (url.contains("/api/tickets")) {
                lastApiUrl = url;
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("""
                                [{
                                  "id": 15,
                                  "title": "<img src=x onerror=alert(1)>",
                                  "project": "<svg onload=alert(1)>",
                                  "assignee": "<script>alert(1)</script>",
                                  "status": "OPEN",
                                  "priority": "HIGH"
                                }]
                                """));
            } else {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("text/html")
                        .setBody(template));
            }
        });

        page.navigate(ORIGIN + "/project/3/tickets");
        page.locator("#ticket-list tr").waitFor();
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

    @Test
    void rendersUntrustedTicketFieldsAsTextWithoutCreatingElements() {
        assertThat(page.locator("#ticket-list a.ticket-link"))
                .hasText("<img src=x onerror=alert(1)>");

        assertThat(page.locator("#ticket-list td").nth(2))
                .hasText("<svg onload=alert(1)>");

        assertThat(page.locator("#ticket-list td").nth(3))
                .hasText("<script>alert(1)</script>");

        assertEquals(0, page.locator("#ticket-list img").count());
        assertEquals(0, page.locator("#ticket-list svg").count());
        assertEquals(0, page.locator("#ticket-list script").count());

        assertEquals(0, page.evaluate("window.__xssExecuted"));
    }

    @Test
    void keepsTicketLinkAndFiltersWorking() {
        assertThat(page.locator("#ticket-list a.ticket-link"))
                .hasAttribute("href", "/project/3/tickets/15");

        page.locator("#status-filter").selectOption("OPEN");

        page.waitForCondition(() ->
            lastApiUrl != null && lastApiUrl.contains("status=OPEN"));

        assertThat(page.locator("#ticket-list tr")).hasCount(1);
        assertEquals(
                ORIGIN + "/api/tickets?status=OPEN",
                lastApiUrl
        );

        page.locator("#priority-filter").selectOption("HIGH");
        page.waitForCondition(() ->
            lastApiUrl != null
                    && lastApiUrl.contains("status=OPEN")
                    && lastApiUrl.contains("priority=HIGH"));
        assertEquals(
                ORIGIN + "/api/tickets?status=OPEN&priority=HIGH",
                lastApiUrl
        );
    }
}