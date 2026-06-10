package com.botica.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {
    // Las imágenes se sirven a través de ControladorArchivos (/uploads/**)
}
