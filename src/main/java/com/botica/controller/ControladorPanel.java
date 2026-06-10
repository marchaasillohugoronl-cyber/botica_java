package com.botica.controller;

import com.botica.service.ServicioProductos;
import com.botica.service.ServicioVentas;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorPanel {

    private final ServicioProductos servicioProductos;
    private final ServicioVentas servicioVentas;

    public ControladorPanel(ServicioProductos servicioProductos, ServicioVentas servicioVentas) {
        this.servicioProductos = servicioProductos;
        this.servicioVentas = servicioVentas;
    }

    @GetMapping({"/", "/dashboard"})
    public String panel(Model model) {
        model.addAttribute("totalProductos", servicioProductos.contarActivos());
        model.addAttribute("stockBajo", servicioProductos.listarStockBajo().size());
        model.addAttribute("porVencer", servicioProductos.listarPorVencer().size());
        model.addAttribute("ventasHoy", servicioVentas.contarVentasHoy());
        model.addAttribute("totalHoy", servicioVentas.totalVentasHoy());
        model.addAttribute("ventasRecientes", servicioVentas.listarRecientes());
        model.addAttribute("productosStockBajo", servicioProductos.listarStockBajo());
        return "dashboard/index";
    }
}
