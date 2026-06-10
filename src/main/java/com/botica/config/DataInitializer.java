package com.botica.config;

import com.botica.model.*;
import com.botica.repository.CategoriaRepositorio;
import com.botica.repository.ProductoRepositorio;
import com.botica.repository.UsuarioRepositorio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepositorio usuarioRepositorio,
                           CategoriaRepositorio categoriaRepositorio,
                           ProductoRepositorio productoRepositorio,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio  = usuarioRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.productoRepositorio  = productoRepositorio;
        this.passwordEncoder      = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        crearAdminSiNoExiste();
        if (categoriaRepositorio.count() == 0) crearCategorias();
        if (productoRepositorio.count() == 0)  crearProductos();
    }

    // ── Admin ────────────────────────────────────────────────
    private void crearAdminSiNoExiste() {
        if (usuarioRepositorio.existsByUsername("admin")) return;
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNombre("Administrador");
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarioRepositorio.save(admin);
    }

    // ── Categorías ───────────────────────────────────────────
    private void crearCategorias() {
        List.of(
            cat("Analgésicos",      "Medicamentos para el dolor"),
            cat("Antibióticos",     "Tratamiento de infecciones bacterianas"),
            cat("Antiinflamatorios","Reducción de inflamación"),
            cat("Vitaminas",        "Suplementos vitamínicos y minerales"),
            cat("Antiácidos",       "Tratamiento de acidez y gastritis"),
            cat("Antialérgicos",    "Tratamiento de alergias"),
            cat("Dermatológicos",   "Cremas y productos para la piel"),
            cat("Otros",            "Productos varios")
        ).forEach(categoriaRepositorio::save);
    }

    private Categoria cat(String nombre, String desc) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion(desc);
        return c;
    }

    // ── Productos ────────────────────────────────────────────
    private void crearProductos() {
        java.util.Map<String, Categoria> cats = new java.util.HashMap<>();
        categoriaRepositorio.findAll().forEach(c -> cats.put(c.getNombre(), c));

        Categoria analgesicos      = cats.get("Analgésicos");
        Categoria antibioticos     = cats.get("Antibióticos");
        Categoria antiinflamatorio = cats.get("Antiinflamatorios");
        Categoria vitaminas        = cats.get("Vitaminas");
        Categoria antiacidos       = cats.get("Antiácidos");
        Categoria antialergicos    = cats.get("Antialérgicos");
        Categoria dermato          = cats.get("Dermatológicos");

        List<Producto> productos = List.of(
            // Analgésicos
            prod("Paracetamol 500mg", "Tableta", "500mg", "Genérico", "7501234560001", analgesicos,
                 new BigDecimal("0.50"), new BigDecimal("0.20"), 200, 20,
                 LocalDate.now().plusYears(2), false),
            prod("Paracetamol 1g", "Tableta", "1000mg", "Genérico", "7501234560002", analgesicos,
                 new BigDecimal("0.80"), new BigDecimal("0.35"), 150, 15,
                 LocalDate.now().plusYears(2), false),
            prod("Tramadol 50mg", "Cápsula", "50mg", "Laboratorios Farma", "7501234560003", analgesicos,
                 new BigDecimal("1.50"), new BigDecimal("0.80"), 80, 10,
                 LocalDate.now().plusMonths(18), true),

            // Antibióticos
            prod("Amoxicilina 500mg", "Cápsula", "500mg", "Pharmedic", "7501234560010", antibioticos,
                 new BigDecimal("1.20"), new BigDecimal("0.60"), 120, 15,
                 LocalDate.now().plusYears(1), true),
            prod("Azitromicina 500mg", "Tableta", "500mg", "Genérico", "7501234560011", antibioticos,
                 new BigDecimal("2.50"), new BigDecimal("1.20"), 90, 10,
                 LocalDate.now().plusYears(1), true),
            prod("Ciprofloxacino 500mg", "Tableta", "500mg", "Laboratorios Roemmers", "7501234560012", antibioticos,
                 new BigDecimal("1.80"), new BigDecimal("0.90"), 100, 12,
                 LocalDate.now().plusMonths(14), true),

            // Antiinflamatorios
            prod("Ibuprofeno 400mg", "Tableta", "400mg", "Genérico", "7501234560020", antiinflamatorio,
                 new BigDecimal("0.60"), new BigDecimal("0.25"), 180, 20,
                 LocalDate.now().plusYears(2), false),
            prod("Diclofenaco 50mg", "Tableta", "50mg", "Pharmedic", "7501234560021", antiinflamatorio,
                 new BigDecimal("0.70"), new BigDecimal("0.30"), 160, 15,
                 LocalDate.now().plusYears(2), false),
            prod("Naproxeno 550mg", "Tableta", "550mg", "Laboratorios Roemmers", "7501234560022", antiinflamatorio,
                 new BigDecimal("1.10"), new BigDecimal("0.50"), 100, 12,
                 LocalDate.now().plusYears(1), false),

            // Vitaminas
            prod("Vitamina C 1g", "Tableta efervescente", "1000mg", "Bayer", "7501234560030", vitaminas,
                 new BigDecimal("1.50"), new BigDecimal("0.70"), 200, 20,
                 LocalDate.now().plusYears(2), false),
            prod("Vitamina D3 2000UI", "Cápsula blanda", "2000UI", "Pharmedic", "7501234560031", vitaminas,
                 new BigDecimal("2.00"), new BigDecimal("1.00"), 120, 15,
                 LocalDate.now().plusYears(2), false),
            prod("Complejo B", "Tableta", "Complejo", "Genérico", "7501234560032", vitaminas,
                 new BigDecimal("1.20"), new BigDecimal("0.55"), 150, 15,
                 LocalDate.now().plusYears(2), false),
            prod("Zinc 50mg", "Tableta", "50mg", "Laboratorios SBN", "7501234560033", vitaminas,
                 new BigDecimal("0.90"), new BigDecimal("0.40"), 130, 12,
                 LocalDate.now().plusYears(2), false),

            // Antiácidos
            prod("Omeprazol 20mg", "Cápsula", "20mg", "Genérico", "7501234560040", antiacidos,
                 new BigDecimal("0.80"), new BigDecimal("0.35"), 160, 20,
                 LocalDate.now().plusYears(1), false),
            prod("Ranitidina 150mg", "Tableta", "150mg", "Pharmedic", "7501234560041", antiacidos,
                 new BigDecimal("0.60"), new BigDecimal("0.28"), 140, 15,
                 LocalDate.now().plusMonths(16), false),
            prod("Hidróxido de Aluminio", "Suspensión oral", "400mg/5ml", "Genérico", "7501234560042", antiacidos,
                 new BigDecimal("8.50"), new BigDecimal("4.00"), 60, 8,
                 LocalDate.now().plusYears(1), false),

            // Antialérgicos
            prod("Loratadina 10mg", "Tableta", "10mg", "Genérico", "7501234560050", antialergicos,
                 new BigDecimal("0.50"), new BigDecimal("0.22"), 200, 20,
                 LocalDate.now().plusYears(2), false),
            prod("Cetirizina 10mg", "Tableta", "10mg", "Pharmedic", "7501234560051", antialergicos,
                 new BigDecimal("0.60"), new BigDecimal("0.28"), 150, 15,
                 LocalDate.now().plusYears(2), false),

            // Dermatológicos
            prod("Clotrimazol Crema 1%", "Tubo 20g", "1%", "Bayer", "7501234560060", dermato,
                 new BigDecimal("12.50"), new BigDecimal("6.00"), 50, 8,
                 LocalDate.now().plusYears(2), false),
            prod("Betametasona Crema 0.1%", "Tubo 15g", "0.1%", "Laboratorios Roemmers", "7501234560061", dermato,
                 new BigDecimal("9.80"), new BigDecimal("4.50"), 40, 6,
                 LocalDate.now().plusYears(2), true)
        );

        productoRepositorio.saveAll(productos);
    }

    private Producto prod(String nombre, String presentacion, String concentracion,
                          String laboratorio, String codigoBarras, Categoria categoria,
                          BigDecimal precioVenta, BigDecimal precioCompra,
                          int stock, int stockMinimo, LocalDate vencimiento, boolean receta) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setPresentacion(presentacion);
        p.setConcentracion(concentracion);
        p.setLaboratorio(laboratorio);
        p.setCodigoBarras(codigoBarras);
        p.setCategoria(categoria);
        p.setPrecioVenta(precioVenta);
        p.setPrecioCompra(precioCompra);
        p.setStock(stock);
        p.setStockMinimo(stockMinimo);
        p.setFechaVencimiento(vencimiento);
        p.setRequiereReceta(receta);
        p.setActivo(true);
        return p;
    }
}
