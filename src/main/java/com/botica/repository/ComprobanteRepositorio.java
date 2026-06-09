package com.botica.repository;

import com.botica.model.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepositorio extends JpaRepository<Comprobante, Long> {

    @Query("SELECT COALESCE(MAX(c.correlativo), 0) FROM Comprobante c WHERE c.serie = :serie")
    Integer findMaxCorrelativoBySerie(String serie);

    Optional<Comprobante> findBySerieAndCorrelativo(String serie, Integer correlativo);
}
