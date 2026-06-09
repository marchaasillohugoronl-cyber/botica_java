package com.botica.service;

import com.botica.dto.VentaDTO;
import com.botica.dto.DetalleVentaDTO;
import com.botica.model.*;
import com.botica.repository.ComprobanteRepositorio;
import com.botica.repository.VentaRepositorio;
import com.botica.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioVentas {

    private final VentaRepositorio ventaRepositorio;
    private final ComprobanteRepositorio comprobanteRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final ServicioProductos servicioProductos;
    private final ServicioSunat servicioSunat;

    @Value("${sunat.serie-factura:F001}")
    private String serieFactura;

    @Value("${sunat.serie-boleta:B001}")
    private String serieBoleta;

    private static final BigDecimal IGV = new BigDecimal("0.18");

    public List<Venta> listarRecientes() {
        return ventaRepositorio.findTop10ByOrderByFechaVentaDesc();
    }

    public List<Venta> listarPorFecha(LocalDateTime desde, LocalDateTime hasta) {
        return ventaRepositorio.findByFechaVentaBetweenOrderByFechaVentaDesc(desde, hasta);
    }

    public Venta buscarPorId(Long id) {
        return ventaRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
    }

    public BigDecimal totalVentasHoy() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);
        return ventaRepositorio.sumTotalByFecha(inicioDia, finDia);
    }

    public Long contarVentasHoy() {
        return ventaRepositorio.countByFechaVentaAfter(LocalDate.now().atStartOfDay());
    }

    @Transactional
    public Venta crearVenta(VentaDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepositorio.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setClienteNombre(dto.getClienteNombre());
        venta.setClienteRucDni(dto.getClienteRucDni());
        venta.setTipoComprobante(dto.getTipoComprobante());
        venta.setObservacion(dto.getObservacion());
        venta.setFechaVenta(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (DetalleVentaDTO detalleDto : dto.getDetalles()) {
            Producto producto = servicioProductos.buscarPorId(detalleDto.getProductoId());

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDto.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            BigDecimal subtotalItem = producto.getPrecioVenta()
                .multiply(new BigDecimal(detalleDto.getCantidad()))
                .setScale(2, RoundingMode.HALF_UP);
            detalle.setSubtotal(subtotalItem);
            venta.getDetalles().add(detalle);

            servicioProductos.actualizarStock(producto.getId(), detalleDto.getCantidad());
            total = total.add(subtotalItem);
        }

        BigDecimal subtotal = total.divide(BigDecimal.ONE.add(IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(subtotal).setScale(2, RoundingMode.HALF_UP);

        venta.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        venta.setSubtotal(subtotal);
        venta.setIgv(igv);

        Venta ventaGuardada = ventaRepositorio.save(venta);

        Comprobante comprobante = crearComprobante(ventaGuardada, dto.getTipoComprobante());
        Comprobante comprobanteEmitido = servicioSunat.emitirComprobante(ventaGuardada, comprobante);
        Comprobante comprobanteGuardado = comprobanteRepositorio.save(comprobanteEmitido);

        ventaGuardada.setComprobante(comprobanteGuardado);
        return ventaRepositorio.save(ventaGuardada);
    }

    private Comprobante crearComprobante(Venta venta, String tipoComprobante) {
        String serie = "01".equals(tipoComprobante) ? serieFactura : serieBoleta;
        Integer correlativo = comprobanteRepositorio.findMaxCorrelativoBySerie(serie) + 1;

        Comprobante comprobante = new Comprobante();
        comprobante.setSerie(serie);
        comprobante.setCorrelativo(correlativo);
        comprobante.setTipoComprobante(tipoComprobante);
        comprobante.setFechaEmision(LocalDate.now());
        comprobante.setSubtotal(venta.getSubtotal());
        comprobante.setIgv(venta.getIgv());
        comprobante.setTotal(venta.getTotal());

        if ("01".equals(tipoComprobante)) {
            comprobante.setClienteRuc(venta.getClienteRucDni());
        } else {
            comprobante.setClienteDni(venta.getClienteRucDni());
        }
        comprobante.setClienteNombre(venta.getClienteNombre());

        return comprobante;
    }
}
