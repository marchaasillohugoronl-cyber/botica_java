package com.botica.service;

import com.botica.model.Categoria;
import com.botica.repository.CategoriaRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicioCategorias {

    private final CategoriaRepositorio categoriaRepositorio;

    public List<Categoria> listarActivas() {
        return categoriaRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public List<Categoria> listarTodas() {
        return categoriaRepositorio.findAll();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepositorio.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + id));
    }

    @Transactional
    public Categoria guardar(Categoria categoria) {
        return categoriaRepositorio.save(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscarPorId(id);
        categoria.setActivo(false);
        categoriaRepositorio.save(categoria);
    }
}
