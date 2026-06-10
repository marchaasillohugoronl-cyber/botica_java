package com.botica.config;

import com.botica.service.ServicioActivityLog;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private final ServicioActivityLog activityLog;

    public SecurityEventListener(ServicioActivityLog activityLog) {
        this.activityLog = activityLog;
    }

    @EventListener
    public void onLoginExitoso(AuthenticationSuccessEvent event) {
        String usuario = event.getAuthentication().getName();
        activityLog.infoComoUsuario(usuario, "AUTH",
            "Inicio de sesión exitoso",
            "Usuario «" + usuario + "» autenticado correctamente");
    }

    @EventListener
    public void onLoginFallido(AbstractAuthenticationFailureEvent event) {
        String usuario = String.valueOf(event.getAuthentication().getPrincipal());
        String causa   = event.getException().getMessage();
        activityLog.warnComoUsuario(usuario, "AUTH",
            "Intento de acceso fallido",
            "Usuario «" + usuario + "» — " + causa);
    }

    @EventListener
    public void onLogout(LogoutSuccessEvent event) {
        String usuario = event.getAuthentication().getName();
        activityLog.infoComoUsuario(usuario, "AUTH",
            "Cierre de sesión",
            "Usuario «" + usuario + "» cerró sesión");
    }
}
