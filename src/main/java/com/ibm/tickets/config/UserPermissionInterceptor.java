package com.ibm.tickets.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ibm.tickets.model.UserRole;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author Rodrigo Costa
 * UserPermissionInterceptor
 */
@Component 
public class UserPermissionInterceptor implements HandlerInterceptor{
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        
            String path = request.getRequestURI()
                                    .substring(request.getContextPath().length());
            
            String method = request.getMethod();

            boolean createUser = "POST".equals(method) 
                                        && path.equals("/api/users");
            boolean deleteUser = "DELETE".equals(method)
                                        && path.equals("/api/users/\\d+");
            boolean managementPage = "GET".equals(method)
                                        && path.equals("/users/manage");

            if(!createUser && !deleteUser && !managementPage)  return true;

            HttpSession session = request.getSession(false);

            if(session == null || session.getAttribute("loggedUser") == null){
                if(path.startsWith("/api/")){
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                } else{
                    response.sendRedirect(request.getContextPath() + "/");
                }

                return false;
            }

            Object role = session.getAttribute("userRole");

            boolean authorized = UserRole.ADMIN.name().equals(role) ||
                UserRole.PROJECT_MANAGER.name().equals(role);

            if(!authorized){
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to manage users."
                );
                return false;
            }

            return true;
    }
}
