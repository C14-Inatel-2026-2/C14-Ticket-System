package com.ibm.tickets.MockedClasses;

import java.util.Enumeration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

public class HttpSessionClassMock implements HttpSession {

    @Override
    public Object getAttribute(String name) {return null;}
    @Override
    public void setAttribute(String name, Object value) {}
    @Override
    public void removeAttribute(String name) {}
    @Override
    public Enumeration<String> getAttributeNames() {return null;}
    @Override
    public long getCreationTime() {return 0;}
    @Override
    public String getId() {return null;}
    @Override
    public long getLastAccessedTime() {return 0;}
    @Override
    public ServletContext getServletContext() {return null;}
    @Override
    public void invalidate() {}
    @Override
    public int getMaxInactiveInterval() {return 0;}
    @Override
    public boolean isNew() {return true;}
    @Override
    public void setMaxInactiveInterval(int arg0) { }
   
}
