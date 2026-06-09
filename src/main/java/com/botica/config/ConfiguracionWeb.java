package com.botica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads/productos}")
    private String directorioSubida;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path rutaAbsoluta = Paths.get(directorioSubida).toAbsolutePath().normalize();
        String ubicacion = "file:" + rutaAbsoluta.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(ubicacion);
    }
}
