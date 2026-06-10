package com.botica.service;

import com.botica.model.ActivityLog;
import com.botica.repository.ActivityLogRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServicioActivityLog {

    private static final Logger log = LoggerFactory.getLogger(ServicioActivityLog.class);

    private final ActivityLogRepositorio repo;

    public ServicioActivityLog(ActivityLogRepositorio repo) {
        this.repo = repo;
    }

    // ── Métodos de conveniencia ─────────────────────────────

    public void info(String modulo, String accion, String detalle) {
        guardar("INFO", modulo, accion, detalle);
    }

    public void warn(String modulo, String accion, String detalle) {
        guardar("WARN", modulo, accion, detalle);
    }

    public void error(String modulo, String accion, String detalle) {
        guardar("ERROR", modulo, accion, detalle);
    }

    // Permite especificar el usuario manualmente (útil en listeners de seguridad)
    public void infoComoUsuario(String usuario, String modulo, String accion, String detalle) {
        guardarComoUsuario("INFO", modulo, usuario, accion, detalle);
    }

    public void warnComoUsuario(String usuario, String modulo, String accion, String detalle) {
        guardarComoUsuario("WARN", modulo, usuario, accion, detalle);
    }

    // ── Consultas ───────────────────────────────────────────

    public List<ActivityLog> obtenerRecientes() {
        return repo.findTop200ByOrderByTimestampDesc();
    }

    public List<ActivityLog> obtenerDesde(LocalDateTime since) {
        return repo.findByTimestampAfterOrderByTimestampAsc(since);
    }

    @Transactional
    public int limpiarAnterioresA(LocalDateTime corte) {
        return repo.eliminarAnterioresA(corte);
    }

    // ── Implementación interna ──────────────────────────────

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void guardar(String nivel, String modulo, String accion, String detalle) {
        try {
            String usuario = resolverUsuarioActual();
            repo.save(new ActivityLog(nivel, modulo, usuario, truncar(accion, 120), truncar(detalle, 500)));
            log.debug("[{}] [{}] {}: {}", nivel, modulo, accion, detalle);
        } catch (Exception e) {
            log.error("Error al guardar ActivityLog: {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void guardarComoUsuario(String nivel, String modulo, String usuario,
                                      String accion, String detalle) {
        try {
            repo.save(new ActivityLog(nivel, modulo, usuario, truncar(accion, 120), truncar(detalle, 500)));
            log.debug("[{}] [{}] {} → {}: {}", nivel, modulo, usuario, accion, detalle);
        } catch (Exception e) {
            log.error("Error al guardar ActivityLog: {}", e.getMessage());
        }
    }

    private String resolverUsuarioActual() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) return auth.getName();
        } catch (Exception ignored) {}
        return "sistema";
    }

    private String truncar(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
