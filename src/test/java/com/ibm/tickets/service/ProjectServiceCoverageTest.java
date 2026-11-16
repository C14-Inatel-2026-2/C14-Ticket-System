package com.ibm.tickets.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.ibm.tickets.dto.*;
import com.ibm.tickets.model.*;
import com.ibm.tickets.repository.*;

/** Tests project mutations and membership boundaries. @author Gabriel Guimaraes */
@ExtendWith(MockitoExtension.class)
class ProjectServiceCoverageTest {
    @Mock ProjectRepository projects;
    @Mock UserRepository users;
    @Mock TicketRepository tickets;
    @InjectMocks ProjectService service;

    @Test void createsTrimmedProjectWithUniqueMembersAndReturnsCatalogCount() {
        User member = user(2L);
        when(users.findAllById(Set.of(2L))).thenReturn(List.of(member));
        when(projects.saveAndFlush(any(Project.class))).thenAnswer(call -> {
            Project saved = call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });
        ProjectCatalogView catalog = row(7L, "Team", 4);
        when(projects.findCatalog()).thenReturn(List.of(catalog));
        assertEquals(new ProjectResponse(7L, "Team", "description", 4),
                service.create(new ProjectRequest("  Team  ", "description", Arrays.asList(2L, 2L))));
        ArgumentCaptor<Project> saved = ArgumentCaptor.forClass(Project.class);
        verify(projects).saveAndFlush(saved.capture());
        assertEquals("Team", saved.getValue().getName());
        assertEquals("description", saved.getValue().getDescription());
        assertEquals(Set.of(member), saved.getValue().getMembers());
    }

    @Test void updatesExistingProjectAndReplacesItsMembers() {
        Project project = project(7L, "Before");
        project.setMembers(Set.of(user(1L)));
        User replacement = user(2L);
        when(projects.findById(7L)).thenReturn(Optional.of(project));
        when(users.findAllById(Set.of(2L))).thenReturn(List.of(replacement));
        ProjectCatalogView catalog = row(7L, "After", 0);
        when(projects.findCatalog()).thenReturn(List.of(catalog));
        assertEquals("After", service.update(7L, new ProjectRequest(" After ", null, Arrays.asList(2L))).name());
        assertEquals("After", project.getName());
        assertNull(project.getDescription());
        assertEquals(Set.of(replacement), project.getMembers());
        verify(projects).saveAndFlush(project);
    }

    @Test void rejectsNullMemberListWithoutPersisting() {
        assertInvalidMembers(null, "Invalid project members");
    }
    @Test void rejectsNullMemberIdWithoutPersisting() {
        assertInvalidMembers(Arrays.asList(1L, null), "Invalid project members");
    }
    @Test void rejectsMissingMemberWithoutPersisting() {
        when(users.findAllById(Set.of(1L, 2L))).thenReturn(List.of(user(1L)));
        assertInvalidMembers(Arrays.asList(1L, 2L), "One or more users do not exist");
    }
    @Test void allowsProjectWithoutMembers() {
        when(users.findAllById(Set.of())).thenReturn(List.of());
        when(projects.saveAndFlush(any(Project.class))).thenAnswer(call -> {
            Project saved = call.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            assertTrue(saved.getMembers().isEmpty());
            return saved;
        });
        ProjectCatalogView catalog = row(7L, "Empty", 0);
        when(projects.findCatalog()).thenReturn(List.of(catalog));
        assertEquals(7L, service.create(new ProjectRequest("Empty", null, new ArrayList<>())).id());
    }
    @Test void rejectsUnknownCatalogProject() {
        ProjectCatalogView catalog = row(7L, "Team", 0);
        when(projects.findCatalog()).thenReturn(List.of(catalog));
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.getProject(99L));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals("Project not found", error.getReason());
    }
    @Test void deletesEmptyProjectAndFlushesDeletion() {
        Project project = project(7L, "Team");
        when(projects.findById(7L)).thenReturn(Optional.of(project));
        service.delete(7L);
        InOrder order = inOrder(projects, tickets);
        order.verify(projects).findById(7L);
        order.verify(tickets).countByProjectReferenceId(7L);
        order.verify(tickets).countByProjectReferenceIsNullAndProject("Team");
        order.verify(projects).delete(project);
        order.verify(projects).flush();
    }
    @Test void blocksDeletionForReferencedTickets() { assertDeleteConflict(1, 0); }
    @Test void blocksDeletionForLegacyTickets() { assertDeleteConflict(0, 1); }
    @Test void returnsSortedMemberIds() {
        Project project = project(7L, "Team");
        project.setMembers(Set.of(user(9L), user(2L)));
        when(projects.findById(7L)).thenReturn(Optional.of(project));
        assertEquals(List.of(2L, 9L), service.getMemberIds(7L));
    }
    @Test void filtersCatalogByMembershipAndKeepsCatalogOrder() {
        when(projects.findDistinctByMembers_IdOrderByIdAsc(2L)).thenReturn(List.of(project(9L, "Nine"), project(7L, "Seven")));
        List<ProjectCatalogView> catalog = List.of(row(3L, "Other", 2), row(7L, "Seven", 4), row(9L, "Nine", 1));
        when(projects.findCatalog()).thenReturn(catalog);
        assertEquals(List.of(new ProjectResponse(7L, "Seven", "description", 4), new ProjectResponse(9L, "Nine", "description", 1)), service.getProjectsByUser(2L));
    }
    private void assertInvalidMembers(List<Long> ids, String reason) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.create(new ProjectRequest("Team", null, ids)));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        assertEquals(reason, error.getReason());
        verify(projects, never()).saveAndFlush(any());
    }
    private void assertDeleteConflict(long referenced, long legacy) {
        when(projects.findById(7L)).thenReturn(Optional.of(project(7L, "Team")));
        when(tickets.countByProjectReferenceId(7L)).thenReturn(referenced);
        when(tickets.countByProjectReferenceIsNullAndProject("Team")).thenReturn(legacy);
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.delete(7L));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Cannot delete a project with tickets", error.getReason());
        verify(projects, never()).delete(any());
        verify(projects, never()).flush();
    }
    private User user(Long id) {
        User user = new User("Member", "member" + id + "@example.com", UserRole.DEVELOPER);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
    private Project project(Long id, String name) {
        Project project = new Project(name, "description");
        ReflectionTestUtils.setField(project, "id", id);
        return project;
    }
    private ProjectCatalogView row(Long id, String name, long count) {
        ProjectCatalogView row = mock(ProjectCatalogView.class);
        when(row.getId()).thenReturn(id);
        when(row.getName()).thenReturn(name);
        when(row.getDescription()).thenReturn("description");
        when(row.getTicketCount()).thenReturn(count);
        return row;
    }
}
