package com.contactme.contact_me_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring configuration class for setting up web-related configurations,
 * such as registering interceptors.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    private final RateLimitInterceptor rateLimitInterceptor;

    /**
     * Constructor for WebConfig.
     * @param rateLimitInterceptor The custom rate limit interceptor to be registered.
     */
    public WebConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
        logger.info("WebConfig initialized with RateLimitInterceptor.");
    }

    /**
     * Registers interceptors with the InterceptorRegistry.
     * The RateLimitInterceptor is added to apply rate limiting to the /api/contact endpoint.
     *
     * @param registry The InterceptorRegistry to add interceptors to.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/contact");
        logger.info("RateLimitInterceptor registered for path pattern: /api/contact");
    }
}