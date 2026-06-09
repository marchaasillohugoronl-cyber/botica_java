package com.botica.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDTO {
    private String clienteNombre;
    private String clienteRucDni;
    private String tipoComprobante; // 01=Factura, 03=Boleta
    private String observacion;
    private List<DetalleVentaDTO> detalles;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
}
