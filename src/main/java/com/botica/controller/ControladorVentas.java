package com.botica.controller;

import com.botica.dto.VentaDTO;
import com.botica.dto.DetalleVentaDTO;
import com.botica.model.Venta;
import com.botica.service.ServicioProductos;
import com.botica.service.ServicioVentas;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class ControladorVentas {

    private final ServicioVentas servicioVentas;
    private final ServicioProductos servicioProductos;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", servicioVentas.listarRecientes());
        return "sales/list";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("productos", servicioProductos.listarActivos());
        return "sales/new";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Venta venta = servicioVentas.buscarPorId(id);
        model.addAttribute("venta", venta);
        return "sales/detail";
    }

    @GetMapping("/{id}/comprobante")
    public String comprobante(@PathVariable Long id, Model model) {
        Venta venta = servicioVentas.buscarPorId(id);
        model.addAttribute("venta", venta);
        model.addAttribute("comprobante", venta.getComprobante());
        return "sales/invoice";
    }

    /**
     * Endpoint AJAX: recibe JSON con los detalles de la venta y la procesa.
     */
    @PostMapping("/procesar")
    @ResponseBody
    public ResponseEntity<?> procesarVenta(@RequestBody VentaDTO dto) {
        try {
            if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La venta debe tener al menos un producto."));
            }

            BigDecimal total = dto.getDetalles().stream()
                .map(d -> {
                    var producto = servicioProductos.buscarPorId(d.getProductoId());
                    return producto.getPrecioVenta().multiply(new BigDecimal(d.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

            BigDecimal subtotal = total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            BigDecimal igv = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

            dto.setTotal(total);
            dto.setSubtotal(subtotal);
            dto.setIgv(igv);

            for (DetalleVentaDTO det : dto.getDetalles()) {
                var producto = servicioProductos.buscarPorId(det.getProductoId());
                if (producto.getStock() < det.getCantidad()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Stock insuficiente para: " + producto.getNombre()
                    ));
                }
            }

            Venta venta = servicioVentas.crearVenta(dto);
            return ResponseEntity.ok(Map.of(
                "ventaId", venta.getId(),
                "mensaje", "Venta registrada correctamente",
                "comprobante", venta.getComprobante() != null
                    ? venta.getComprobante().getSerie() + "-" + String.format("%08d", venta.getComprobante().getCorrelativo())
                    : "SIN COMPROBANTE"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
