package com.botica.config;

import com.botica.model.Categoria;
import com.botica.model.Producto;
import com.botica.model.Rol;
import com.botica.model.Usuario;
import com.botica.repository.CategoriaRepositorio;
import com.botica.repository.ProductoRepositorio;
import com.botica.repository.UsuarioRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class InicializadorDatos implements CommandLineRunner {

    private final UsuarioRepositorio usuarioRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final PasswordEncoder codificadorPassword;

    @Override
    public void run(String... args) {
        if (usuarioRepositorio.count() == 0) {
            crearUsuarios();
            crearCategorias();
            crearProductosDemo();
            log.info("=== Datos iniciales cargados ===");
            log.info("Usuario ADMIN: admin / admin123");
            log.info("Usuario VENDEDOR: vendedor / vendedor123");
        }
    }

    private void crearUsuarios() {
        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(codificadorPassword.encode("admin123"));
        admin.setNombre("Administrador del Sistema");
        admin.setEmail("admin@botica.com");
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarioRepositorio.save(admin);

        Usuario vendedor = new Usuario();
        vendedor.setUsername("vendedor");
        vendedor.setPassword(codificadorPassword.encode("vendedor123"));
        vendedor.setNombre("Juan Pérez");
        vendedor.setEmail("vendedor@botica.com");
        vendedor.setRol(Rol.VENDEDOR);
        vendedor.setActivo(true);
        usuarioRepositorio.save(vendedor);
    }

    private void crearCategorias() {
        String[][] categorias = {
            {"Analgésicos", "Medicamentos para el dolor"},
            {"Antibióticos", "Tratamiento de infecciones bacterianas"},
            {"Antiinflamatorios", "Reducción de inflamación"},
            {"Vitaminas y Suplementos", "Complementos nutricionales"},
            {"Antiácidos", "Para problemas digestivos y acidez"},
            {"Antialérgicos", "Tratamiento de alergias"},
            {"Dermatológicos", "Cuidado de la piel"},
            {"Cuidado Personal", "Higiene y cuidado personal"}
        };
        for (String[] c : categorias) {
            Categoria categoria = new Categoria();
            categoria.setNombre(c[0]);
            categoria.setDescripcion(c[1]);
            categoriaRepositorio.save(categoria);
        }
    }

    private void crearProductosDemo() {
        Categoria analgesicos = categoriaRepositorio.findByActivoTrueOrderByNombreAsc()
            .stream().filter(c -> c.getNombre().equals("Analgésicos")).findFirst().orElse(null);
        Categoria vitaminas = categoriaRepositorio.findByActivoTrueOrderByNombreAsc()
            .stream().filter(c -> c.getNombre().equals("Vitaminas y Suplementos")).findFirst().orElse(null);
        Categoria antibioticos = categoriaRepositorio.findByActivoTrueOrderByNombreAsc()
            .stream().filter(c -> c.getNombre().equals("Antibióticos")).findFirst().orElse(null);

        Object[][] productos = {
            {"Paracetamol 500mg x20", "PARA500", analgesicos, "Laboratorio Chile", "Tabletas", "500mg", new BigDecimal("3.50"), new BigDecimal("2.00"), 100, 10},
            {"Ibuprofeno 400mg x10", "IBU400", analgesicos, "Bayer", "Cápsulas", "400mg", new BigDecimal("6.90"), new BigDecimal("4.00"), 80, 10},
            {"Amoxicilina 500mg x21", "AMOX500", antibioticos, "GlaxoSmithKline", "Cápsulas", "500mg", new BigDecimal("18.00"), new BigDecimal("12.00"), 50, 5},
            {"Vitamina C 1000mg x30", "VITC1000", vitaminas, "Redoxon", "Tabletas", "1000mg", new BigDecimal("25.00"), new BigDecimal("15.00"), 60, 8},
            {"Omeprazol 20mg x14", "OMP20", null, "Grunenthal", "Cápsulas", "20mg", new BigDecimal("12.50"), new BigDecimal("7.00"), 70, 10},
            {"Loratadina 10mg x10", "LOR10", null, "Schering-Plough", "Tabletas", "10mg", new BigDecimal("8.90"), new BigDecimal("5.00"), 45, 5},
        };

        for (Object[] p : productos) {
            Producto producto = new Producto();
            producto.setNombre((String) p[0]);
            producto.setCodigoBarras((String) p[1]);
            producto.setCategoria((Categoria) p[2]);
            producto.setLaboratorio((String) p[3]);
            producto.setPresentacion((String) p[4]);
            producto.setConcentracion((String) p[5]);
            producto.setPrecioVenta((BigDecimal) p[6]);
            producto.setPrecioCompra((BigDecimal) p[7]);
            producto.setStock((Integer) p[8]);
            producto.setStockMinimo((Integer) p[9]);
            producto.setFechaVencimiento(LocalDate.now().plusMonths(18));
            productoRepositorio.save(producto);
        }
    }
}
