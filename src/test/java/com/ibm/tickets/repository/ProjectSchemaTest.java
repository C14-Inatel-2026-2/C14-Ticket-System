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
        jdbc.update("insert into projects (name) values (?)", "Ticket System");
        jdbc.update("insert into projects (name) values (?)", "C14 Ticket System");
        assertEquals(2, jdbc.queryForObject("select count(*) from projects", Integer.class));
        assertNull(jdbc.queryForObject("select description from projects where name = ?",
                String.class, "Ticket System"));
    }

    @Test
    void requiresName() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("insert into projects (name) values (null)"));
    }

    @Test
    void acceptsExistingNameLimit() {
        jdbc.update("insert into projects (name) values (?)", "x".repeat(100));
        assertEquals("x".repeat(100), jdbc.queryForObject("select name from projects", String.class));
    }

    @Test
    void rejectsNameAboveExistingLimit() {
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("insert into projects (name) values (?)", "x".repeat(101)));
    }
}
