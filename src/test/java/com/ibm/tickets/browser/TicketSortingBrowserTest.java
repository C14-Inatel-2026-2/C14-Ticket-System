package com.ibm.tickets.browser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

class TicketSortingBrowserTest {

    private static final String ORIGIN = "http://127.0.0.1:18788";

    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    private String lastApiUrl;

    private static final String TICKETS_JSON = """
        [
          {
            "id": 1,
            "title": "Ticket LOW",
            "project": "Ticket System",
            "projectId": 3,
            "assignee": "Ana",
            "status": "OPEN",
            "priority": "LOW",
            "createdAt": "2026-01-10T10:00:00Z"
          },
          {
            "id": 2,
            "title": "Ticket URGENT",
            "project": "Ticket System",
            "projectId": 3,
            "assignee": "Bruno",
            "status": "OPEN",
            "priority": "URGENT",
            "createdAt": "2026-01-12T10:00:00Z"
          },
          {
            "id": 3,
            "title": "Ticket HIGH",
            "project": "Ticket System",
            "projectId": 3,
            "assignee": "Carlos",
            "status": "OPEN",
            "priority": "HIGH",
            "createdAt": "2026-01-11T10:00:00Z"
          },
          {
            "id": 4,
            "title": "Ticket MEDIUM",
            "project": "Ticket System",
            "projectId": 3,
            "assignee": "Daniela",
            "status": "OPEN",
            "priority": "MEDIUM",
            "createdAt": "2026-01-09T10:00:00Z"
          }
        ]
        """;

    @BeforeEach
    void openTicketList() throws Exception {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setChannel(System.getProperty("playwright.channel", "chromium"))
        );

        context = browser.newContext();
        page = context.newPage();

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
                        .setBody(TICKETS_JSON));
            } else {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("text/html")
                        .setBody(template));
            }
        });

        page.navigate(ORIGIN + "/project/3/tickets");

        page.locator("#ticket-list tr").first().waitFor();
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

    private void selectSorting(String option) {
        page.locator("#sort-filter").selectOption(option);
        page.waitForCondition(() ->
                lastApiUrl != null && lastApiUrl.contains("/api/tickets") && lastApiUrl.contains("projectId=3"));
    }

    private List<String> getTicketIds() {
        return page.locator("#ticket-list tr td:first-child").allTextContents();
    }

    @Test
    void sortsTicketsByCreationDateDescending() {
        selectSorting("created-desc");

        assertEquals(List.of("2", "3", "1", "4"), getTicketIds());
    }

    @Test
    void sortsTicketsByCreationDateAscending() {
        selectSorting("created-asc");

        assertEquals(List.of("4", "1", "3", "2"), getTicketIds());
    }

    @Test
    void sortsTicketsByPriorityDescending() {
        selectSorting("priority-desc");

        assertEquals(List.of("2", "3", "4", "1"), getTicketIds());
    }

    @Test
    void sortsTicketsByPriorityAscending() {
        selectSorting("priority-asc");

        assertEquals(List.of("1", "4", "3", "2"), getTicketIds());
    }

    @Test
    void preservesFiltersWhenChangingSorting() {
        page.locator("#status-filter").selectOption("OPEN");

        page.waitForCondition(() ->
                lastApiUrl != null && lastApiUrl.contains("projectId=3") && lastApiUrl.contains("status=OPEN"));

        page.locator("#sort-filter").selectOption("priority-desc");

        page.waitForCondition(() ->
                lastApiUrl != null && lastApiUrl.contains("projectId=3") && lastApiUrl.contains("status=OPEN"));

        assertTrue(lastApiUrl.contains("projectId=3"));
        assertTrue(lastApiUrl.contains("status=OPEN"));

        assertEquals(List.of("2", "3", "4", "1"), getTicketIds());
    }

    @Test
    void displaysCreationDateInsteadOfProject() {
        assertThat(page.locator("thead th").nth(2))
                .hasText("Data de Criação");

        assertThat(page.locator("#ticket-list tr")
                .first()
                .locator("td")
                .nth(2))
                .hasText("12/01/2026");
    }
}
