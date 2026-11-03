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
    void migratesFreshDatabaseWithDefaultProjects() throws Exception {
        String url = databaseUrl();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            Flyway.configure().dataSource(url, "sa", "").load().migrate();
            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("select name, description from projects order by name")) {
                assertTrue(rows.next());
                assertEquals("C14 Ticket System", rows.getString(1));
                assertEquals("Sistema de gerenciamento de tickets.", rows.getString(2));
                assertTrue(rows.next());
                assertEquals("OpenLift", rows.getString(1));
                assertEquals("Aplicativo de gerenciamento de academias.", rows.getString(2));
                assertTrue(rows.next());
                assertEquals("Portal Interno", rows.getString(1));
                assertEquals("Portal de serviços e solicitações internas.", rows.getString(2));
                assertFalse(rows.next());
            }
            assertEquals(0, Flyway.configure().dataSource(url, "sa", "").load().migrate().migrationsExecuted);
        }
    }

    @Test
    void seedsOnlyMissingExactNamesWithoutOverwritingExistingProjects() throws Exception {
        String url = databaseUrl();
        try (var connection = DriverManager.getConnection(url, "sa", "")) {
            Flyway.configure().dataSource(url, "sa", "").target("6").load().migrate();
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("""
                        insert into projects (name, description) values
                        ('C14 Ticket System', 'Descrição personalizada'),
                        ('c14 ticket system', 'Lowercase'),
                        (' C14 Ticket System ', 'Com espaços')
                        """);
            }

            var result = Flyway.configure().dataSource(url, "sa", "").load().migrate();
            assertEquals(1, result.migrationsExecuted);

            try (var statement = connection.createStatement();
                 var rows = statement.executeQuery("""
                         select name, description from projects order by name
                         """)) {
                assertProject(rows, " C14 Ticket System ", "Com espaços");
                assertProject(rows, "C14 Ticket System", "Descrição personalizada");
                assertProject(rows, "OpenLift", "Aplicativo de gerenciamento de academias.");
                assertProject(rows, "Portal Interno", "Portal de serviços e solicitações internas.");
                assertProject(rows, "c14 ticket system", "Lowercase");
                assertFalse(rows.next());
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
            assertEquals(3, result.migrationsExecuted);
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
                assertEquals(7, rows.getInt(1));
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

    private void assertProject(java.sql.ResultSet rows, String name, String description) throws SQLException {
        assertTrue(rows.next());
        assertEquals(name, rows.getString(1));
        assertEquals(description, rows.getString(2));
    }
}
