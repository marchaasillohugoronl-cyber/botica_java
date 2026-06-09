package com.botica.controller;

import com.botica.service.ServicioProductos;
import com.botica.service.ServicioVentas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ControladorPanel {

    private final ServicioProductos servicioProductos;
    private final ServicioVentas servicioVentas;

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
