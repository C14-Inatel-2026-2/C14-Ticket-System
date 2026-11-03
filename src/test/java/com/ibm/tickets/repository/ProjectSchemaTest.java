package com.ibm.tickets.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the project migration using the actual database schema.
 * @author Gabriel Guimaraes
 */
@DataJpaTest
class ProjectSchemaTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void storesNamesWithoutMergingAndAllowsOptionalDescription() {
        jdbc.update("insert into projects (name) values (?)", "Schema Ticket System");
        jdbc.update("insert into projects (name) values (?)", "Schema C14 Ticket System");
        assertEquals(2, jdbc.queryForObject("""
                select count(*) from projects where name in (?, ?)
                """, Integer.class, "Schema Ticket System", "Schema C14 Ticket System"));
        assertNull(jdbc.queryForObject("select description from projects where name = ?",
                String.class, "Schema Ticket System"));
    }

    @Test
    void requiresName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("insert into projects (name) values (null)"));
    }

    @Test
    void acceptsExistingNameLimit() {
        String name = "x".repeat(100);
        jdbc.update("insert into projects (name) values (?)", name);
        assertEquals(name, jdbc.queryForObject(
                "select name from projects where name = ?", String.class, name));
    }

    @Test
    void rejectsNameAboveExistingLimit() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("insert into projects (name) values (?)", "x".repeat(101)));
    }
}
