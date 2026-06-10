package com.botica.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionWeb.class);

    @Value("${app.upload.dir:./uploads/productos}")
    private String directorioSubida;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rutaAbsoluta = Paths.get(directorioSubida).toAbsolutePath().normalize();
        String ubicacion = rutaAbsoluta.toUri().toString();
        if (!ubicacion.endsWith("/")) ubicacion += "/";

        log.info("Sirviendo imágenes desde: {}", ubicacion);

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(ubicacion);
    }
}
