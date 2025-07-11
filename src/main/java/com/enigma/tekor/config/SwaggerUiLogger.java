package com.enigma.tekor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
@Profile("!prod")
public class SwaggerUiLogger implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerPath;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        WebServerApplicationContext ctx = (WebServerApplicationContext) event.getApplicationContext();
        int port = ctx.getWebServer().getPort();

        String url = "http://localhost:" + port + contextPath + swaggerPath;

        log.info("\n----------------------------------------------------------\n"
                + "  Swagger UI available at {}\n"
                + "----------------------------------------------------------", url);
    }
}
