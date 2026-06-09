package com.botica.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 100)
    private String codigoBarras;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(length = 100)
    private String laboratorio;

    @Column(length = 50)
    private String presentacion;

    @Column(length = 50)
    private String concentracion;

    @Column(length = 20)
    private String unidadMedida = "UND";

    @NotNull
    @DecimalMin("0.00")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(precision = 10, scale = 2)
    private BigDecimal precioCompra = BigDecimal.ZERO;

    @Min(0)
    @Column(nullable = false)
    private Integer stock = 0;

    @Min(0)
    @Column(nullable = false)
    private Integer stockMinimo = 5;

    private LocalDate fechaVencimiento;

    @Column(length = 255)
    private String imagenPath;

    private boolean requiereReceta = false;

    @Column(nullable = false)
    private boolean activo = true;
}
