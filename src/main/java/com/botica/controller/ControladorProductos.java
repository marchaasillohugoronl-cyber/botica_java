package com.botica.controller;

import com.botica.model.Producto;
import com.botica.service.ServicioCategorias;
import com.botica.service.ServicioProductos;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ControladorProductos {

    private final ServicioProductos servicioProductos;
    private final ServicioCategorias servicioCategorias;

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.isBlank()) {
            model.addAttribute("productos", servicioProductos.buscar(q));
            model.addAttribute("q", q);
        } else {
            model.addAttribute("productos", servicioProductos.listarActivos());
        }
        return "products/list";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String nuevo(Model model) {
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", servicioCategorias.listarActivas());
        return "products/form";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("producto", servicioProductos.buscarPorId(id));
        model.addAttribute("categorias", servicioCategorias.listarActivas());
        return "products/form";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardar(@Valid @ModelAttribute Producto producto,
                          BindingResult resultado,
                          @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
                          Model model,
                          RedirectAttributes flash) {
        if (resultado.hasErrors()) {
            model.addAttribute("categorias", servicioCategorias.listarActivas());
            return "products/form";
        }
        try {
            servicioProductos.guardar(producto, imagenFile);
            flash.addFlashAttribute("exito", "Producto guardado correctamente.");
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminar(@PathVariable Long id, RedirectAttributes flash) {
        servicioProductos.desactivar(id);
        flash.addFlashAttribute("exito", "Producto desactivado.");
        return "redirect:/productos";
    }

    @GetMapping("/api/buscar")
    @ResponseBody
    public Object buscarJson(@RequestParam String q) {
        return servicioProductos.buscar(q).stream().map(p -> new Object() {
            public final Long id = p.getId();
            public final String nombre = p.getNombre();
            public final String presentacion = p.getPresentacion();
            public final double precio = p.getPrecioVenta().doubleValue();
            public final int stock = p.getStock();
        }).toList();
    }
}
