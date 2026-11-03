package com.ibm.tickets.repository;

/**
 * Read-only projection for the project catalog.
 * @author Gabriel Guimaraes
 */
public interface ProjectCatalogView {
    Long getId();

    String getName();

    String getDescription();

    long getTicketCount();
}
