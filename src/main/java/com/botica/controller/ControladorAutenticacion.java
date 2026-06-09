package com.botica.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ControladorAutenticacion {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String expired,
                        Model model) {
        if (error != null) model.addAttribute("error", "Usuario o contraseña incorrectos.");
        if (logout != null) model.addAttribute("mensaje", "Sesión cerrada correctamente.");
        if (expired != null) model.addAttribute("error", "Su sesión ha expirado. Por favor, ingrese nuevamente.");
        return "auth/login";
    }
}
