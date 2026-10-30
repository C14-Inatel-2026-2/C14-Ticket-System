package com.ibm.tickets.repository;

import java.sql.DriverManager;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies fresh installation and additive upgrade without modifying existing tickets.
 * @author Gabriel Guimaraes
 */
class ProjectMigrationTest {
    @Test
    void migratesFreshDatabaseWithoutSeedingProjects() throws Exception {
        String url = databaseUrl();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select count(*) from projects")) {
                assertTrue(rows.next());
                assertEquals(0, rows.getInt(1));
            }
        }
    }

    @Test
    void upgradesVersionFourWithoutChangingLegacyTicket() throws Exception {
        String url = databaseUrl();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            Flyway.configure().dataSource(url, "sa", "").target("4").load().migrate();
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("""
                        insert into tickets (title, description, project, assignee)
                        values ('Legacy ticket', 'Keep description', ' Ticket System ', 'Owner')
                        """);
            }
            var result = Flyway.configure().dataSource(url, "sa", "").load().migrate();
            assertEquals(1, result.migrationsExecuted);
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select title, description, project, assignee from tickets")) {
                assertTrue(rows.next());
                assertEquals("Legacy ticket", rows.getString(1));
                assertEquals("Keep description", rows.getString(2));
                assertEquals(" Ticket System ", rows.getString(3));
                assertEquals("Owner", rows.getString(4));
                assertFalse(rows.next());
            }
            assertEquals(0, Flyway.configure().dataSource(url, "sa", "").load().migrate().migrationsExecuted);
        }
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:projects_" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    }
}
