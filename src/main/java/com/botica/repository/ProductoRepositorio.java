package com.botica.repository;

import com.botica.model.Categoria;
import com.botica.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    List<Producto> findByCategoriaAndActivoTrue(Categoria categoria);

    Optional<Producto> findByCodigoBarras(String codigoBarras);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.laboratorio) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Producto> buscar(String q);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo")
    List<Producto> findStockBajo();

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.fechaVencimiento IS NOT NULL AND p.fechaVencimiento <= :fechaLimite")
    List<Producto> findPorVencer(@Param("fechaLimite") LocalDate fechaLimite);
}
