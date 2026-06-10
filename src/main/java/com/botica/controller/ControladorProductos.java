package com.botica.controller;

import com.botica.model.Producto;
import com.botica.service.ServicioCategorias;
import com.botica.service.ServicioProductos;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/productos")
public class ControladorProductos {

    private final ServicioProductos servicioProductos;
    private final ServicioCategorias servicioCategorias;

    public ControladorProductos(ServicioProductos servicioProductos, ServicioCategorias servicioCategorias) {
        this.servicioProductos = servicioProductos;
        this.servicioCategorias = servicioCategorias;
    }

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

    @GetMapping("/api/todos")
    @ResponseBody
    public List<Map<String, Object>> todosJson() {
        return toJson(servicioProductos.listarActivos().stream());
    }

    @GetMapping("/api/buscar")
    @ResponseBody
    public List<Map<String, Object>> buscarJson(@RequestParam String q) {
        if (q == null || q.isBlank()) return todosJson();
        return toJson(servicioProductos.buscar(q).stream());
    }

    @GetMapping("/api/por-categoria")
    @ResponseBody
    public List<Map<String, Object>> porCategoriaJson(@RequestParam Long cat) {
        return toJson(servicioProductos.listarPorCategoria(cat).stream());
    }

    @GetMapping("/api/mas-vendidos")
    @ResponseBody
    public List<Map<String, Object>> masVendidosJson(@RequestParam(defaultValue = "16") int limite) {
        return toJson(servicioProductos.listarMasVendidos(limite).stream());
    }

    private List<Map<String, Object>> toJson(Stream<Producto> stream) {
        return stream.map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",           p.getId());
            m.put("nombre",       p.getNombre());
            m.put("presentacion", p.getPresentacion());
            m.put("concentracion",p.getConcentracion());
            m.put("laboratorio",  p.getLaboratorio());
            m.put("categoria",    p.getCategoria() != null ? p.getCategoria().getNombre() : null);
            m.put("precio",       p.getPrecioVenta().doubleValue());
            m.put("stock",        p.getStock());
            m.put("imagenUrl",    p.getImagenPath() != null ? "/uploads/" + p.getImagenPath() : null);
            return m;
        }).toList();
    }
}
