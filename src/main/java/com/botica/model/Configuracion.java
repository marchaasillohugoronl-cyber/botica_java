package com.botica.model;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 100)
    private String farmaciaName = "Botica Salud";

    @Column(length = 150)
    private String farmaciaSubtitle = "Sistema de Gestión Farmacéutico";

    @Column(length = 255)
    private String logoPath;

    @Column(length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 11)
    private String ruc;

    public Configuracion() {}

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }

    public String getFarmaciaName()          { return farmaciaName; }
    public void setFarmaciaName(String v)    { this.farmaciaName = v; }

    public String getFarmaciaSubtitle()      { return farmaciaSubtitle; }
    public void setFarmaciaSubtitle(String v){ this.farmaciaSubtitle = v; }

    public String getLogoPath()              { return logoPath; }
    public void setLogoPath(String v)        { this.logoPath = v; }

    public String getDireccion()             { return direccion; }
    public void setDireccion(String v)       { this.direccion = v; }

    public String getTelefono()              { return telefono; }
    public void setTelefono(String v)        { this.telefono = v; }

    public String getRuc()                   { return ruc; }
    public void setRuc(String v)             { this.ruc = v; }
}
