package com.ibm.tickets.model;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * @author Rodrigo Fraga da Costa
 * TicketActivity
 */
@Entity 
@Table(name = "ticket_activities")
public class TicketActivity {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column (nullable = false, length = 100)
    private String author;

    @Column(name = "activity_type", nullable = false, length = 50)
    private String activityType;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected TicketActivity() {}

    public TicketActivity(
            Ticket ticket,
            String author,
            String activityType,
            String oldValue,
            String newValue) {
        this.ticket = ticket;
        this.author = author;
        this.activityType = activityType;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @PrePersist 
    private void beforeInsert(){
        if(createdAt == null){
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() { return id; }
    public Ticket getTicket() { return ticket; }
    public String getAuthor() { return author; }
    public String getActivityType() { return activityType; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
