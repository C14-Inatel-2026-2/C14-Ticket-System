package com.ibm.tickets.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.tickets.dto.TicketRequest;
import com.ibm.tickets.dto.TicketResponse;
import com.ibm.tickets.exception.TicketNotFoundException;
import com.ibm.tickets.exception.TicketNotInProjectException;
import com.ibm.tickets.model.Project;
import com.ibm.tickets.model.Ticket;
import com.ibm.tickets.model.TicketPriority;
import com.ibm.tickets.model.TicketStatus;
import com.ibm.tickets.repository.ProjectRepository;
import com.ibm.tickets.repository.TicketRepository;


/**
 * Provides the business operations for tickets.
 *
 * @author Gabriel Guimaraes
 */
@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketActivityService activityService;
    private final ProjectRepository projectRepository;

    public TicketService(
            TicketRepository ticketRepository,
            TicketActivityService activityService,
            ProjectRepository projectRepository) {
        this.ticketRepository = ticketRepository;
        this.activityService = activityService;
        this.projectRepository = projectRepository;
    }
    
    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found: " + projectId));
    }

    @Transactional
    public TicketResponse create(TicketRequest request) {
        Ticket ticket = new Ticket(
                request.title(),
                request.description(),
                request.priority());

        ticket.setProjectReference(findProjectById(request.projectId()));
        ticket.setAssignee(request.assignee());

        Ticket savedTicket = ticketRepository.saveAndFlush(ticket);
        return TicketResponse.from(savedTicket);
    }

    public List<TicketResponse> findAll(TicketStatus status) {
        return findAll(status, null);
    }

    public List<TicketResponse> findAll(TicketStatus status, TicketPriority priority) {
        return findTickets(null, status, priority).stream()
                .map(TicketResponse::from)
                .toList();
    }

    public List<TicketResponse> findAll(Long projectId, TicketStatus status, TicketPriority priority) {
        return findTickets(projectId, status, priority).stream()
                .map(TicketResponse::from).toList();
    }

    private List<Ticket> findTickets(Long projectId, TicketStatus status, TicketPriority priority) {
        List<Ticket> tickets;

        if (projectId == null) {
            if (status == null && priority == null) {
                return ticketRepository.findAllByOrderByCreatedAtDesc();
            } else if (status != null && priority == null) {
                return ticketRepository
                    .findAllByStatusOrderByCreatedAtDesc(status);
            } else if (status == null) {
                return ticketRepository
                    .findAllByPriorityOrderByCreatedAtDesc(priority);
            } else {
                return ticketRepository
                    .findAllByStatusAndPriorityOrderByCreatedAtDesc(status, priority);
            }
        }

        if (status == null && priority == null) {
            tickets = ticketRepository.findAllByProjectReference_IdOrderByCreatedAtDesc(projectId);
        } else if (status != null && priority == null) {
            tickets = ticketRepository
                .findAllByProjectReference_IdAndStatusOrderByCreatedAtDesc(projectId, status);
        } else if (status == null) {
            tickets = ticketRepository
                .findAllByProjectReference_IdAndPriorityOrderByCreatedAtDesc(projectId, priority);
        } else {
            tickets = ticketRepository
                .findAllByProjectReference_IdAndStatusAndPriorityOrderByCreatedAtDesc(projectId, status, priority);
        }

        return tickets;
    }

    public TicketResponse findById(Long id) {
        return TicketResponse.from(findEntityById(id));
    }

    public TicketResponse findByIdInProject(Long ticketId, Long projectId) {
        TicketResponse ticket = findById(ticketId);

        if (ticket.projectId() == null || !ticket.projectId().equals(projectId)) {
            throw new TicketNotInProjectException();
        }

        return ticket;
    }

    @Transactional
    public TicketResponse update(Long id, TicketRequest request, String author) {
        Ticket ticket = findEntityById(id);

        String oldAssignee = ticket.getAssignee();
        String oldPriority = ticket.getPriority().name();

        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setProjectReference(findProjectById(request.projectId()));
        ticket.setAssignee(request.assignee());
        ticket.setPriority(request.priority());

        Ticket updatedTicket = ticketRepository.saveAndFlush(ticket);


        activityService.record(updatedTicket,
                               author, 
                               "ASSIGNEE_CHANGED",
                               oldAssignee,
                               updatedTicket.getAssignee());
            
        activityService.record(updatedTicket,
                               author, 
                               "PRIORITY_CHANGED",
                               oldPriority,
                               updatedTicket.getPriority().name());

        return TicketResponse.from(updatedTicket);
    }

    @Transactional
    public TicketResponse updateStatus(
            Long id,
            TicketStatus status, String author) {
        Ticket ticket = findEntityById(id);
        TicketStatus previousStatus = ticket.getStatus();

        if (previousStatus == status) {
            return TicketResponse.from(ticket);
        }

        ticket.setStatus(status);

        Ticket updatedTicket = ticketRepository.saveAndFlush(ticket);

        activityService.record(
            updatedTicket,
            author,
            "STATUS_CHANGED",
            previousStatus.name(),
            status.name()
        );

        return TicketResponse.from(updatedTicket);
    }

    @Transactional
    public void delete(Long id) {
        Ticket ticket = findEntityById(id);
        ticketRepository.delete(ticket);
    }

    private Ticket findEntityById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }
}
