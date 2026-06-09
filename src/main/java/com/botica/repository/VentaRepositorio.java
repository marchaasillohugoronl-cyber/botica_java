package com.botica.repository;

import com.botica.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    List<Venta> findTop10ByOrderByFechaVentaDesc();

    List<Venta> findByFechaVentaBetweenOrderByFechaVentaDesc(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fechaVenta >= :desde AND v.fechaVenta <= :hasta")
    BigDecimal sumTotalByFecha(LocalDateTime desde, LocalDateTime hasta);

    @Query("SELECT COUNT(v) FROM Venta v WHERE v.estado = 'COMPLETADA' AND v.fechaVenta >= :desde")
    Long countByFechaVentaAfter(LocalDateTime desde);
}
