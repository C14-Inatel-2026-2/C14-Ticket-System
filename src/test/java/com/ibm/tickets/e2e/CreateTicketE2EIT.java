package com.ibm.tickets.e2e;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicInteger;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Real login, HTTP API and PostgreSQL persistence on a dedicated loopback test instance.
 * @author C14 Team
 */
@EnabledIfSystemProperty(named = "c14.e2e", matches = "true")
@ActiveProfiles("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateTicketE2EIT {
    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void dedicatedDatabase(DynamicPropertyRegistry registry) {
        int dbPort = Integer.parseInt(System.getenv().getOrDefault("C14_E2E_DB_PORT", "55432"));
        if (dbPort < 1024 || dbPort > 65535) throw new IllegalArgumentException("Invalid E2E DB port");
        String password = System.getenv("C14_E2E_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Set C14_E2E_DB_PASSWORD for the dedicated local database");
        }
        registry.add("spring.datasource.url", () ->
                "jdbc:postgresql://127.0.0.1:" + dbPort + "/c14_ticket_e2e");
        registry.add("spring.datasource.username", () -> "c14_ticket_e2e");
        registry.add("spring.datasource.password", () -> password);
    }

    @Test
    void rejectsInvalidThenPersistsTicketThroughRealUserFlow() throws Exception {
        String title = "e2e-" + UUID.randomUUID();
        String origin = "http://127.0.0.1:" + port;
        AtomicInteger posts = new AtomicInteger();
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setChannel(System.getProperty("playwright.channel", "chromium")));
             BrowserContext context = browser.newContext()) {
            Page page = context.newPage();
            try {
                page.navigate(origin + "/");
                // Existing demo login, used only against the test instance started by this class.
                page.locator("#username").fill("admin");
                page.locator("#password").fill("admin");
                page.locator("button[type=submit]").click();
                assertThat(page).hasURL(Pattern.compile("^http://127[.]0[.]0[.]1:" + port + "/tickets(?:;jsessionid=[A-Za-z0-9]+)?$"));
                page.navigate(origin + "/tickets/new");
                page.onRequest(request -> {
                    if (request.method().equals("POST") && request.url().endsWith("/api/tickets")) {
                        posts.incrementAndGet();
                    }
                });
                page.locator("#title").fill(title);
                page.locator("#project").selectOption("Ticket System");
                page.locator("#priority").selectOption("HIGH");
                page.locator("#description").fill("Dados exclusivos do teste local");
                page.locator("#owner").fill("   ");
                page.locator("button[type=submit]").click();
                assertThat(page.locator("#owner-error")).isVisible();
                page.screenshot(new Page.ScreenshotOptions()
                        .setPath(Path.of("target/browser-artifacts/e2e-invalid-form.png")).setFullPage(true));
                assertEquals(0, posts.get());
                assertEquals(0, jdbc.queryForObject("select count(*) from tickets where title = ?",
                        Integer.class, title));

                page.locator("#owner").fill("Pessoa de teste E2E");
                page.locator("button[type=submit]").click();
                assertThat(page).hasURL(origin + "/tickets");
                assertThat(page.getByText(title, new Page.GetByTextOptions().setExact(true))).isVisible();
                page.reload();
                assertThat(page.getByText(title, new Page.GetByTextOptions().setExact(true))).isVisible();
                assertEquals(1, posts.get());
                assertEquals(1, jdbc.queryForObject("select count(*) from tickets where title = ?",
                        Integer.class, title));
            } catch (Exception | AssertionError failure) {
                Path image = Path.of("target/browser-artifacts/e2e-failure-" + UUID.randomUUID() + ".png");
                try {
                    Files.createDirectories(image.getParent());
                    page.screenshot(new Page.ScreenshotOptions().setPath(image).setFullPage(true));
                } catch (Exception ignored) { /* Keep the original failure. */ }
                throw failure;
            }
        } finally {
            // Only this test's uniquely named ticket, in the dedicated test database.
            jdbc.update("delete from tickets where title = ?", title);
        }
    }
}
