package com.ibm.tickets.exception;

/**
 * Exception thrown when a ticket cannot be found.
 *
 * @author Gabriel Guimaraes
 */
public class TicketNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TicketNotFoundException(Long id) {
        super("Ticket not found with id: " + id);
    }
}