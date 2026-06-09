package com.botica.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4)
    private String serie;

    @Column(nullable = false)
    private Integer correlativo;

    @Column(nullable = false, length = 2)
    private String tipoComprobante; // 01=Factura, 03=Boleta

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

    // Estado: PENDIENTE, ACEPTADO, RECHAZADO, ANULADO
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
}
