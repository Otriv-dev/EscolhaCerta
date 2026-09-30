package br.com.escolhacerta.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.nio.file.Path;

@Configuration public class WebConfig implements WebMvcConfigurer {
    @Value("${app.storage.local-path}") private String path;
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**").addResourceLocations(Path.of(path).toAbsolutePath().toUri().toString().replaceAll("/+$", "") + "/").setCachePeriod(3600);
    }
}
