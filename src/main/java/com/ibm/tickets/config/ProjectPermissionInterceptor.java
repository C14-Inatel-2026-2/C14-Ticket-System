package com.ibm.tickets.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ibm.tickets.model.UserRole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Rodrigo Costa
 * ProjectPermissionInterceptor
 */
@Component 
public class ProjectPermissionInterceptor implements HandlerInterceptor {
    

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception{
            String path = request.getRequestURI().substring(request.getContextPath().length());

            String method = request.getMethod();

            boolean modifyApi = 
                ("POST".equals(method) && path.equals("/api/projects"))
                || (("PUT".equals(method) || "DELETE".equals(method))
                    && path.matches("/api/projects/\\d+"));

            boolean modifyingPage = 
                "GET".equals(method)
                && (path.equals("/project/new")
                    || path.matches("/project/\\d+/edit"));

            if(!modifyApi && !modifyingPage) return true;

            HttpSession session = request.getSession();

            if(session == null || session.getAttribute("loggedUser") == null){
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            String role = String.valueOf(session.getAttribute("userRole"));

            boolean allowed = UserRole.ADMIN.name().equals(role)
                || UserRole.PROJECT_MANAGER.name().equals(role);
            
            if(!allowed){
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You do not have the permission to manage projects"
                );

                return false;
            }

            return true;
    }
}
