package com.botica.controller;

import com.botica.model.Categoria;
import com.botica.service.ServicioCategorias;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ControladorCategorias {

    private final ServicioCategorias servicioCategorias;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", servicioCategorias.listarTodas());
        return "categories/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categories/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("categoria", servicioCategorias.buscarPorId(id));
        return "categories/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute Categoria categoria,
                          BindingResult resultado, RedirectAttributes flash) {
        if (resultado.hasErrors()) return "categories/form";
        servicioCategorias.guardar(categoria);
        flash.addFlashAttribute("exito", "Categoría guardada.");
        return "redirect:/categorias";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        servicioCategorias.eliminar(id);
        flash.addFlashAttribute("exito", "Categoría desactivada.");
        return "redirect:/categorias";
    }
}
