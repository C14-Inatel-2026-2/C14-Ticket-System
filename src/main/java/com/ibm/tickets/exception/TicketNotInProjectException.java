package com.ibm.tickets.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Mariana Barude Pina
 * Exception thrown when a ticket does not belong to the requested project.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TicketNotInProjectException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TicketNotInProjectException() {
        super("Ticket not found in this project");
    }
}
