package com.ibm.tickets.dto;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
                "Internal Systems",
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
                "Internal Systems",
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