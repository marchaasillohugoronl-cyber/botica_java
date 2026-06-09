package com.botica.service;

import com.botica.model.Producto;
import com.botica.repository.ProductoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioProductos {

    private final ProductoRepositorio productoRepositorio;
    private final ServicioAlmacenamiento servicioAlmacenamiento;

    public List<Producto> listarActivos() {
        return productoRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public List<Producto> buscar(String q) {
        return productoRepositorio.buscar(q);
    }

    public Producto buscarPorId(Long id) {
        return productoRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
    }

    public List<Producto> listarStockBajo() {
        return productoRepositorio.findStockBajo();
    }

    public List<Producto> listarPorVencer() {
        return productoRepositorio.findPorVencer(LocalDate.now().plusDays(30));
    }

    @Transactional
    public Producto guardar(Producto producto, MultipartFile imagen) throws IOException {
        if (imagen != null && !imagen.isEmpty()) {
            if (producto.getImagenPath() != null) {
                servicioAlmacenamiento.eliminarImagen(producto.getImagenPath());
            }
            String nombreArchivo = servicioAlmacenamiento.guardarImagen(imagen);
            producto.setImagenPath(nombreArchivo);
        }
        return productoRepositorio.save(producto);
    }

    @Transactional
    public void desactivar(Long id) {
        Producto producto = buscarPorId(id);
        producto.setActivo(false);
        productoRepositorio.save(producto);
    }

    @Transactional
    public void actualizarStock(Long productoId, int cantidad) {
        Producto producto = buscarPorId(productoId);
        int nuevoStock = producto.getStock() - cantidad;
        if (nuevoStock < 0) {
            throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
        }
        producto.setStock(nuevoStock);
        productoRepositorio.save(producto);
    }

    public long contarActivos() {
        return productoRepositorio.findByActivoTrueOrderByNombreAsc().size();
    }
}
