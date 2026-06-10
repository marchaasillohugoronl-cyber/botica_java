package com.botica.config;

import com.botica.model.Configuracion;
import com.botica.service.ServicioConfiguracion;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ConfiguracionAdvice {

    private final ServicioConfiguracion servicioConfiguracion;

    public ConfiguracionAdvice(ServicioConfiguracion servicioConfiguracion) {
        this.servicioConfiguracion = servicioConfiguracion;
    }

    @ModelAttribute("appConfig")
    public Configuracion appConfig(HttpServletRequest request) {
        if (esApiRequest(request)) return null;
        return servicioConfiguracion.obtener();
    }

    @ModelAttribute("logoUrl")
    public String logoUrl(HttpServletRequest request) {
        if (esApiRequest(request)) return null;
        Configuracion c = servicioConfiguracion.obtener();
        return c != null ? servicioConfiguracion.resolverLogoUrl(c.getLogoPath()) : null;
    }

    private boolean esApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("/api/") || uri.startsWith("/ventas/procesar");
    }
}
