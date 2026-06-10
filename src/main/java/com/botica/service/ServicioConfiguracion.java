package com.botica.service;

import com.botica.model.Configuracion;
import com.botica.repository.ConfiguracionRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicioConfiguracion {

    private final ConfiguracionRepositorio repo;

    public ServicioConfiguracion(ConfiguracionRepositorio repo) {
        this.repo = repo;
    }

    public Configuracion obtener() {
        return repo.findById(1L).orElseGet(() -> {
            Configuracion c = new Configuracion();
            return repo.save(c);
        });
    }

    @Transactional
    public void guardar(Configuracion config) {
        config.setId(1L);
        repo.save(config);
    }

    @Transactional
    public void actualizarLogo(String logoPath) {
        Configuracion c = obtener();
        c.setLogoPath(logoPath);
        repo.save(c);
    }

    public String resolverLogoUrl(String logoPath) {
        if (logoPath == null || logoPath.isBlank()) return null;
        if (logoPath.startsWith("http")) return logoPath;
        return "/uploads/" + logoPath;
    }
}
