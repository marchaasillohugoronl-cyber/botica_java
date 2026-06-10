package com.botica.controller;

import com.botica.model.ActivityLog;
import com.botica.model.Configuracion;
import com.botica.model.Rol;
import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepositorio;
import com.botica.service.ServicioActivityLog;
import com.botica.service.ServicioAlmacenamiento;
import com.botica.service.ServicioConfiguracion;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/configuracion")
@PreAuthorize("hasRole('ADMIN')")
public class ControladorConfiguracion {

    private final ServicioConfiguracion servicioConfiguracion;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ServicioAlmacenamiento servicioAlmacenamiento;
    private final PasswordEncoder passwordEncoder;
    private final ServicioActivityLog activityLog;

    public ControladorConfiguracion(ServicioConfiguracion servicioConfiguracion,
                                    UsuarioRepositorio usuarioRepositorio,
                                    ServicioAlmacenamiento servicioAlmacenamiento,
                                    PasswordEncoder passwordEncoder,
                                    ServicioActivityLog activityLog) {
        this.servicioConfiguracion  = servicioConfiguracion;
        this.usuarioRepositorio     = usuarioRepositorio;
        this.servicioAlmacenamiento = servicioAlmacenamiento;
        this.passwordEncoder        = passwordEncoder;
        this.activityLog            = activityLog;
    }

    // ── Página principal ─────────────────────────────────────
    @GetMapping
    public String index(Model model) {
        model.addAttribute("config",   servicioConfiguracion.obtener());
        model.addAttribute("usuarios", usuarioRepositorio.findAll());
        model.addAttribute("logs",     activityLog.obtenerRecientes());
        return "configuracion/index";
    }

    // ── Farmacia ─────────────────────────────────────────────
    @PostMapping("/farmacia")
    public String guardarFarmacia(@RequestParam String farmaciaName,
                                  @RequestParam(required = false) String farmaciaSubtitle,
                                  @RequestParam(required = false) String ruc,
                                  @RequestParam(required = false) String direccion,
                                  @RequestParam(required = false) String telefono,
                                  RedirectAttributes flash) {
        Configuracion c = servicioConfiguracion.obtener();
        c.setFarmaciaName(farmaciaName.trim());
        c.setFarmaciaSubtitle(farmaciaSubtitle != null ? farmaciaSubtitle.trim() : "");
        c.setRuc(ruc != null ? ruc.trim() : null);
        c.setDireccion(direccion != null ? direccion.trim() : null);
        c.setTelefono(telefono != null ? telefono.trim() : null);
        servicioConfiguracion.guardar(c);
        activityLog.info("CONFIG", "Información de farmacia actualizada",
            "Nombre: " + farmaciaName.trim());
        flash.addFlashAttribute("exito", "Información de la farmacia actualizada.");
        return "redirect:/configuracion";
    }

    // ── Logo ─────────────────────────────────────────────────
    @PostMapping("/logo")
    public String subirLogo(@RequestParam("logoFile") MultipartFile logoFile,
                            RedirectAttributes flash) {
        if (logoFile.isEmpty()) {
            flash.addFlashAttribute("warning", "Selecciona un archivo de imagen.");
            return "redirect:/configuracion";
        }
        try {
            Configuracion c = servicioConfiguracion.obtener();
            if (c.getLogoPath() != null && !c.getLogoPath().isBlank()) {
                servicioAlmacenamiento.eliminarImagen(c.getLogoPath());
            }
            String path = servicioAlmacenamiento.guardarImagen(logoFile);
            servicioConfiguracion.actualizarLogo(path);
            activityLog.info("CONFIG", "Logo actualizado", logoFile.getOriginalFilename());
            flash.addFlashAttribute("exito", "Logo actualizado correctamente.");
        } catch (Exception e) {
            activityLog.error("CONFIG", "Error al subir logo", e.getMessage());
            flash.addFlashAttribute("error", "Error al subir el logo: " + e.getMessage());
        }
        return "redirect:/configuracion";
    }

    @PostMapping("/logo/eliminar")
    public String eliminarLogo(RedirectAttributes flash) {
        Configuracion c = servicioConfiguracion.obtener();
        if (c.getLogoPath() != null) {
            servicioAlmacenamiento.eliminarImagen(c.getLogoPath());
            c.setLogoPath(null);
            servicioConfiguracion.guardar(c);
            activityLog.warn("CONFIG", "Logo eliminado", "—");
        }
        flash.addFlashAttribute("exito", "Logo eliminado.");
        return "redirect:/configuracion";
    }

    // ── Usuarios ─────────────────────────────────────────────
    @PostMapping("/usuarios/nuevo")
    public String crearVendedor(@RequestParam String username,
                                @RequestParam String nombre,
                                @RequestParam(required = false) String email,
                                @RequestParam String password,
                                RedirectAttributes flash) {
        String user = username.trim().toLowerCase();
        if (usuarioRepositorio.existsByUsername(user)) {
            flash.addFlashAttribute("error", "El usuario '" + user + "' ya existe.");
            return "redirect:/configuracion?tab=usuarios";
        }
        if (password.length() < 6) {
            flash.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/configuracion?tab=usuarios";
        }
        Usuario u = new Usuario();
        u.setUsername(user);
        u.setNombre(nombre.trim());
        u.setEmail(email != null && !email.isBlank() ? email.trim() : null);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(Rol.VENDEDOR);
        u.setActivo(true);
        usuarioRepositorio.save(u);
        activityLog.info("CONFIG", "Vendedor creado — " + nombre.trim(),
            "Usuario: " + user + (email != null ? " | " + email : ""));
        flash.addFlashAttribute("exito", "Vendedor «" + nombre.trim() + "» creado correctamente.");
        return "redirect:/configuracion?tab=usuarios";
    }

    @PostMapping("/usuarios/{id}/toggle")
    public String toggleUsuario(@PathVariable Long id, RedirectAttributes flash) {
        usuarioRepositorio.findById(id).ifPresent(u -> {
            u.setActivo(!u.isActivo());
            usuarioRepositorio.save(u);
            String estado = u.isActivo() ? "activado" : "desactivado";
            activityLog.info("CONFIG", "Usuario " + estado + " — " + u.getNombre(),
                "Username: " + u.getUsername());
            flash.addFlashAttribute("exito", u.getNombre() + " " + estado + ".");
        });
        return "redirect:/configuracion?tab=usuarios";
    }

    @PostMapping("/usuarios/{id}/reset-password")
    public String resetPassword(@PathVariable Long id,
                                @RequestParam String newPassword,
                                RedirectAttributes flash) {
        if (newPassword.length() < 6) {
            flash.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/configuracion?tab=usuarios";
        }
        usuarioRepositorio.findById(id).ifPresent(u -> {
            u.setPassword(passwordEncoder.encode(newPassword));
            usuarioRepositorio.save(u);
            activityLog.warn("CONFIG", "Contraseña restablecida — " + u.getNombre(),
                "Username: " + u.getUsername());
            flash.addFlashAttribute("exito", "Contraseña de " + u.getNombre() + " actualizada.");
        });
        return "redirect:/configuracion?tab=usuarios";
    }

    // ── API Activity Log (polling) ────────────────────────────
    @GetMapping("/actividad/api")
    @ResponseBody
    public ResponseEntity<?> getActividad(@RequestParam(required = false) Long since) {
        LocalDateTime sinceTime = since != null
            ? LocalDateTime.ofInstant(Instant.ofEpochMilli(since), ZoneId.systemDefault())
            : LocalDateTime.now().minusMinutes(10);

        List<ActivityLog> logs = activityLog.obtenerDesde(sinceTime);

        List<Map<String, Object>> payload = logs.stream().map(l -> Map.<String, Object>of(
            "id",        l.getId(),
            "timestamp", l.getTimestamp().toString(),
            "epochMs",   l.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            "nivel",     l.getNivel(),
            "modulo",    l.getModulo(),
            "usuario",   l.getUsuario() != null ? l.getUsuario() : "—",
            "accion",    l.getAccion(),
            "detalle",   l.getDetalle() != null ? l.getDetalle() : ""
        )).toList();

        return ResponseEntity.ok(payload);
    }

    @PostMapping("/actividad/limpiar")
    public String limpiarLogs(@RequestParam(defaultValue = "7") int dias,
                               RedirectAttributes flash) {
        LocalDateTime corte = LocalDateTime.now().minusDays(dias);
        int eliminados = activityLog.limpiarAnterioresA(corte);
        activityLog.warn("SISTEMA", "Logs eliminados",
            eliminados + " entradas anteriores a " + dias + " días");
        flash.addFlashAttribute("exito", eliminados + " entradas de log eliminadas.");
        return "redirect:/configuracion?tab=actividad";
    }
}
