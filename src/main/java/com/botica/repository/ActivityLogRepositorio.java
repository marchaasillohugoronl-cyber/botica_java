package com.botica.repository;

import com.botica.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityLogRepositorio extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findTop200ByOrderByTimestampDesc();

    List<ActivityLog> findByTimestampAfterOrderByTimestampAsc(LocalDateTime since);

    @Modifying
    @Query("DELETE FROM ActivityLog a WHERE a.timestamp < :corte")
    int eliminarAnterioresA(@Param("corte") LocalDateTime corte);
}
