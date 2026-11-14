package com.ibm.tickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Rodrigo Fraga da Costa
 * WebConfig
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthenticationInterceptor authenticationInterceptor;
    private final ProjectPermissionInterceptor projectPermissionInterceptor;

    private final UserPermissionInterceptor userPermissionInterceptor;

    public WebConfig(AuthenticationInterceptor authenticationInterceptor,
        UserPermissionInterceptor userPermissionInterceptor,
        ProjectPermissionInterceptor projectPermissionInterceptor
    ) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.userPermissionInterceptor = userPermissionInterceptor;
        this.projectPermissionInterceptor = projectPermissionInterceptor;
    }

    @Override
    public void addInterceptors( 
        InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
        .addPathPatterns("/tickets/**",
         "/api/tickets/**",
          "/users/**",
           "/api/users/**",
           "/project/**",
           "/api/projects",
           "/api/projects/**",
           "/js/**");

        registry.addInterceptor(userPermissionInterceptor)
            .addPathPatterns("/users/manage",
                "/api/users",
                "/api/users/*"
            );

        registry.addInterceptor(projectPermissionInterceptor)
            .addPathPatterns("/project/**",
                        "/api/projects",
                        "/api/projects/**");
    }


}
