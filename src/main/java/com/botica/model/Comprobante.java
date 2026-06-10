package com.botica.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes")
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false)
    private Integer correlativo;

    @Column(nullable = false, length = 2)
    private String tipoComprobante;

    @Column(length = 11)
    private String clienteRuc;

    @Column(length = 8)
    private String clienteDni;

    @Column(length = 200)
    private String clienteNombre;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 20)
    private String moneda = "PEN";

    @Column(nullable = false, length = 20)
    private String estadoSunat = "PENDIENTE";

    @Column(columnDefinition = "TEXT")
    private String xmlContent;

    @Column(columnDefinition = "TEXT")
    private String cdrContent;

    @Column(length = 500)
    private String sunatMensaje;

    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public Comprobante() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public Integer getCorrelativo() { return correlativo; }
    public void setCorrelativo(Integer correlativo) { this.correlativo = correlativo; }

    public String getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(String tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public String getClienteRuc() { return clienteRuc; }
    public void setClienteRuc(String clienteRuc) { this.clienteRuc = clienteRuc; }

    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getEstadoSunat() { return estadoSunat; }
    public void setEstadoSunat(String estadoSunat) { this.estadoSunat = estadoSunat; }

    public String getXmlContent() { return xmlContent; }
    public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }

    public String getCdrContent() { return cdrContent; }
    public void setCdrContent(String cdrContent) { this.cdrContent = cdrContent; }

    public String getSunatMensaje() { return sunatMensaje; }
    public void setSunatMensaje(String sunatMensaje) { this.sunatMensaje = sunatMensaje; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}
