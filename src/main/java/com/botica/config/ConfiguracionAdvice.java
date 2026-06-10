package com.botica.config;

import com.botica.model.Configuracion;
import com.botica.service.ServicioConfiguracion;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ConfiguracionAdvice {

    private final ServicioConfiguracion servicioConfiguracion;

    public ConfiguracionAdvice(ServicioConfiguracion servicioConfiguracion) {
        this.servicioConfiguracion = servicioConfiguracion;
    }

    @ModelAttribute("appConfig")
    public Configuracion appConfig() {
        return servicioConfiguracion.obtener();
    }

    @ModelAttribute("logoUrl")
    public String logoUrl() {
        return servicioConfiguracion.resolverLogoUrl(servicioConfiguracion.obtener().getLogoPath());
    }
}
