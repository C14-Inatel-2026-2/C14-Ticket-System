package com.ibm.tickets.dto;

import java.time.OffsetDateTime;
import com.ibm.tickets.model.TicketActivity;
import com.ibm.tickets.model.TicketComment;

/**
 * @author Rodrigo Fraga da Costa
 * TicketActivityResponse
 * @param type
 * @param author
 * @param activityType
 * @param oldValue
 * @param newValue
 * @param content
 * @param createdAt
 */
public record TicketActivityResponse(
    String type,
    String author,
    String activityType,
    String oldValue,
    String newValue,
    String content,
    OffsetDateTime createdAt
) {
    public static TicketActivityResponse fromActivity(TicketActivity activity) {
        return new TicketActivityResponse(
                "history",
                activity.getAuthor(),
                activity.getActivityType(),
                activity.getOldValue(),
                activity.getNewValue(),
                null,
                activity.getCreatedAt());
    }

    public static TicketActivityResponse fromComment(TicketComment comment) {
        return new TicketActivityResponse(
                "comment",
                comment.getAuthor(),
                "COMMENT",
                null,
                null,
                comment.getContent(),
                comment.getCreatedAt());
    }
}
