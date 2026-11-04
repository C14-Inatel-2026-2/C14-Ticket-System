package com.ibm.tickets.repository;

import java.sql.DriverManager;
import java.sql.SQLException;
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
            assertEquals(2, result.migrationsExecuted);
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("""
                         select t.title, t.description, t.project, t.assignee, p.name
                         from tickets t join projects p on p.id = t.project_id
                         """)) {
                assertTrue(rows.next());
                assertEquals("Legacy ticket", rows.getString(1));
                assertEquals("Keep description", rows.getString(2));
                assertEquals(" Ticket System ", rows.getString(3));
                assertEquals("Owner", rows.getString(4));
                assertEquals(" Ticket System ", rows.getString(5));
                assertFalse(rows.next());
            }
            assertEquals(0, Flyway.configure().dataSource(url, "sa", "").load().migrate().migrationsExecuted);
        }
    }

    @Test
    void reusesExactProjectsAndKeepsDifferentNamesSeparate() throws Exception {
        String url = databaseUrl();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            Flyway.configure().dataSource(url, "sa", "").target("5").load().migrate();
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("insert into projects (name) values ('Ticket System')");
                statement.executeUpdate("insert into projects (name) values ('Ticket System')");
                statement.executeUpdate("""
                        insert into tickets (title, description, project, assignee) values
                        ('First', 'Description', 'Ticket System', 'Owner'),
                        ('Second', 'Description', 'Ticket System', 'Owner'),
                        ('Third', 'Description', 'C14 Ticket System', 'Owner'),
                        ('Spaced', 'Description', ' Ticket System ', 'Owner'),
                        ('Lower', 'Description', 'ticket system', 'Owner')
                        """);
            }

            Flyway.configure().dataSource(url, "sa", "").load().migrate();

            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("""
                         select count(*), count(distinct project_id), min(project_id), max(project_id)
                         from tickets where project = 'Ticket System'
                         """)) {
                assertTrue(rows.next());
                assertEquals(2, rows.getInt(1));
                assertEquals(1, rows.getInt(2));
                assertEquals(rows.getLong(3), rows.getLong(4));
            }
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select count(*) from projects")) {
                assertTrue(rows.next());
                assertEquals(5, rows.getInt(1));
            }
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("""
                         select count(distinct p.name)
                         from tickets t join projects p on p.id = t.project_id
                         """)) {
                assertTrue(rows.next());
                assertEquals(4, rows.getInt(1));
            }
            assertThrows(SQLException.class, () -> {
                try (var statement = connection.createStatement()) {
                    statement.executeUpdate("update tickets set project_id = 999999 where title = 'First'");
                }
            });
        }
    }

    private String databaseUrl() {
        return "jdbc:h2:mem:projects_" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
    }
}
