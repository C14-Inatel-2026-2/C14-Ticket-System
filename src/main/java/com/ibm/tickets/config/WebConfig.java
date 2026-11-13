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

    public WebConfig(AuthenticationInterceptor authenticationInterceptor,
            ProjectPermissionInterceptor projectPermissionInterceptor
    ) {
        this.authenticationInterceptor = authenticationInterceptor;
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

        registry.addInterceptor(projectPermissionInterceptor)
            .addPathPatterns("/project/**",
                        "/api/projects",
                        "/api/projects/**");
    }


}
