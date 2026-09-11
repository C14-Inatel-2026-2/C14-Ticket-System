package com.ibm.tickets.model;

/**
 * Ticket
 * @author Mariana Barude Pina
 */

public class Ticket {

    private Integer id;
    private String name;
    private String project;
    private String assignee;
    private Status status;
    private Priority priority;

    public Ticket(Integer id, String name, String project, String assignee, Status status, Priority priority){
        this.id = id;
        this.name = name;
        this.project = project;
        this.assignee = assignee;
        this.status = status;
        this.priority = priority;
    }

    public void setId(Integer id){
        this.id = id;
    }
    public Integer getId(){
        return this.id;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }

    public void setProject(String project){
        this.project = project;
    }
    public String getProject(){
        return this.project;
    }

    public void setAssignee(String assignee){
        this.assignee = assignee;
    }
    public String getAssignee(){
        return this.assignee;
    }

    public void setStatus(Status status){
        this.status = status;
    }
    public Status getStatus(){
        return this.status;
    }

    public void setPriority(Priority priority){
        this.priority = priority;
    }
    public Priority getPriority(){
        return this.priority;
    }

}
