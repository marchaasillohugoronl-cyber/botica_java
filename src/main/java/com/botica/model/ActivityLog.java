package com.botica.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_al_timestamp", columnList = "timestamp"),
    @Index(name = "idx_al_nivel",     columnList = "nivel")
})
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 5)
    private String nivel;           // INFO | WARN | ERROR

    @Column(nullable = false, length = 15)
    private String modulo;          // VENTAS | PRODUCTOS | CONFIG | AUTH | SISTEMA

    @Column(length = 50)
    private String usuario;

    @Column(nullable = false, length = 120)
    private String accion;

    @Column(length = 500)
    private String detalle;

    public ActivityLog() {}

    public ActivityLog(String nivel, String modulo, String usuario, String accion, String detalle) {
        this.timestamp = LocalDateTime.now();
        this.nivel     = nivel;
        this.modulo    = modulo;
        this.usuario   = usuario;
        this.accion    = accion;
        this.detalle   = detalle;
    }

    public Long           getId()        { return id; }
    public LocalDateTime  getTimestamp() { return timestamp; }
    public String         getNivel()     { return nivel; }
    public String         getModulo()    { return modulo; }
    public String         getUsuario()   { return usuario; }
    public String         getAccion()    { return accion; }
    public String         getDetalle()   { return detalle; }

    public void setId(Long id)                    { this.id = id; }
    public void setTimestamp(LocalDateTime ts)     { this.timestamp = ts; }
    public void setNivel(String nivel)             { this.nivel = nivel; }
    public void setModulo(String modulo)           { this.modulo = modulo; }
    public void setUsuario(String usuario)         { this.usuario = usuario; }
    public void setAccion(String accion)           { this.accion = accion; }
    public void setDetalle(String detalle)         { this.detalle = detalle; }
}
