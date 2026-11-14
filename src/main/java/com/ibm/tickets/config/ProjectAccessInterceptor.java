
package com.ibm.tickets.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.repository.TicketRepository;
import com.ibm.tickets.service.ProjectAccessService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Rodrigo Costs
 * ProjectAccessInterceptor
 */
@Component
public class ProjectAccessInterceptor implements HandlerInterceptor {

    private static final Pattern WEB_TICKETS = Pattern.compile(
        "^/project/(\\d+)/tickets(?:/.*)?$"
    );

    private static final Pattern TICKET_RESOURCES = Pattern.compile(
        "^/api/tickets/(\\d+)/(?:comments|activities)(?:/.*)?$"
    );

    private final ProjectAccessService projectAccessService;
    private final TicketRepository ticketRepository;

    public ProjectAccessInterceptor(
            ProjectAccessService projectAccessService,
            TicketRepository ticketRepository) {

        this.projectAccessService = projectAccessService;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String path = request.getRequestURI()
            .substring(request.getContextPath().length());

        Matcher webMatcher = WEB_TICKETS.matcher(path);

        if (webMatcher.matches()) {
            Long projectId = Long.valueOf(webMatcher.group(1));

            projectAccessService.checkAccess(
                projectId,
                request.getSession(false)
            );

            return true;
        }

        Matcher resourceMatcher =
            TICKET_RESOURCES.matcher(path);

        if (resourceMatcher.matches()) {
            Long ticketId = Long.valueOf(
                resourceMatcher.group(1)
            );

            Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                    new TicketNotFoundException(ticketId)
                );

            if (ticket.getProjectReference() == null) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ticket has no associated project"
                );
            }

            projectAccessService.checkAccess(
                ticket.getProjectReference().getId(),
                request.getSession(false)
            );
        }

        return true;
    }
}
