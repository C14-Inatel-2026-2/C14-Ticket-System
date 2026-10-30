/**
 * Returns the current project ID from the URL.
 *
 * Example:
 * /project/3/tickets/15 -> 3
 */
function getProjectId() {
    const match = window.location.pathname.match(
        /^\/project\/(\d+)\/tickets(?:\/.*)?\/?$/
    );

    if (!match) {
        throw new Error("Project ID not found in URL");
    }

    return match[1];
}

/**
 * Returns the ticket list URL for the current project.
 */
function getTicketsUrl() {
    return `/project/${getProjectId()}/tickets`;
}

/**
 * Returns the new ticket page URL.
 */
function getNewTicketUrl() {
    return `${getTicketsUrl()}/new`;
}

/**
 * Returns the details page URL for a ticket.
 */
function getTicketDetailsUrl(ticketId) {
    return `${getTicketsUrl()}/${ticketId}`;
}

/**
 * Redirects to the current project's ticket list.
 */
function backToTickets() {
    window.location.href = getTicketsUrl();
}

function getTicketEditUrl(ticketId) {
    return `${getTicketsUrl()}/${ticketId}/edit`;
}