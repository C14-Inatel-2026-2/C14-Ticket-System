package com.ibm.tickets.dto;


/**
 * 
 * @author Rodrigo Costa
 * 
 * @param id project identifier
 * @param name project name
 * @param description little description
 * @param ticketCount project total number of tickets
 */

public record ProjectResponse(
    Long id,
    String name,
    String description,
    int ticketCount
) {}