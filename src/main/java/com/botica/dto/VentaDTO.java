package com.botica.dto;

import java.math.BigDecimal;
import java.util.List;

public class VentaDTO {
    private String clienteNombre;
    private String clienteRucDni;
    private String tipoComprobante;
    private String observacion;
    private List<DetalleVentaDTO> detalles;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;

    public VentaDTO() {}

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteRucDni() { return clienteRucDni; }
    public void setClienteRucDni(String clienteRucDni) { this.clienteRucDni = clienteRucDni; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public List<DetalleVentaDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleVentaDTO> detalles) { this.detalles = detalles; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
}
