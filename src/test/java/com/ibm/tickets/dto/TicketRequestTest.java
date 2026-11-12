package com.ibm.tickets.dto;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ibm.tickets.model.TicketPriority;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Unit tests for TicketRequest validation.
 *
 * @author Gabriel Guimaraes
 */
class TicketRequestTest {

    static Stream<Arguments> invalidFields() {
        return Stream.of(
                Arguments.of("title", null), Arguments.of("title", ""),
                Arguments.of("title", "   "), Arguments.of("title", "x".repeat(151)),
                Arguments.of("assignee", null), Arguments.of("assignee", ""),
                Arguments.of("assignee", "   "), Arguments.of("assignee", "x".repeat(101)),
                Arguments.of("projectId", null), Arguments.of("description", null), 
                Arguments.of("description", ""), Arguments.of("description", "   "));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void rejectsInvalidFieldWithoutDependingOnBrowser(String field, String value) {
        TicketRequest request = new TicketRequest(
                field.equals("title") ? value : "Valid title",
                field.equals("description") ? value : "Valid description",
                field.equals("projectId") ? null : 1L,
                field.equals("assignee") ? value : "Valid owner", TicketPriority.HIGH);
        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals(field)));
    }

    @Test
    void acceptsExactLimitsAndDoesNotInventDescriptionLimit() {
        TicketRequest request = new TicketRequest("t".repeat(150), "d".repeat(2000),
                1L, "a".repeat(100), TicketPriority.HIGH);
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsMissingPriority() {
        TicketRequest request = new TicketRequest("Title", "Description", 1L, "Owner", null);
        assertTrue(validator.validate(request).stream()
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("priority")));
    }

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptValidTicketRequest() {
        TicketRequest request = new TicketRequest(
                "Printer problem",
                "The printer is not working",
                1L,
                "Gabriel",
                TicketPriority.HIGH);

        Set<ConstraintViolation<TicketRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldRejectTicketRequestWithBlankTitle() {
        TicketRequest request = new TicketRequest(
                "",
                "The printer is not working",
                1L,
                "Gabriel",
                TicketPriority.HIGH);

        Set<ConstraintViolation<TicketRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(violation ->
                        violation.getPropertyPath()
                                .toString()
                                .equals("title")
                        && violation.getMessage()
                                .equals("Title is required")));
    }
}
