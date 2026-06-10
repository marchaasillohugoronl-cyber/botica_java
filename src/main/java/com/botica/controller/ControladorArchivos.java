package com.botica.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Controller
public class ControladorArchivos {

    private static final Logger log = LoggerFactory.getLogger(ControladorArchivos.class);

    private static final Map<String, String> TIPOS = Map.of(
        "jpg",  "image/jpeg",
        "jpeg", "image/jpeg",
        "png",  "image/png",
        "gif",  "image/gif",
        "webp", "image/webp"
    );

    @Value("${app.upload.dir:./uploads/productos}")
    private String directorioSubida;

    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> servirImagen(@PathVariable String filename) {
        try {
            Path archivo = Paths.get(directorioSubida).toAbsolutePath().normalize().resolve(filename);
            log.info("[UPLOADS] Solicitado: {} | Buscando en: {} | Existe: {}",
                filename, archivo, Files.exists(archivo));
            if (!Files.exists(archivo) || !Files.isReadable(archivo)) {
                return ResponseEntity.notFound().build();
            }
            String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase()
                : "jpg";
            String contentType = TIPOS.getOrDefault(ext, "application/octet-stream");
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=86400")
                .body(new FileSystemResource(archivo));
        } catch (Exception e) {
            log.error("[UPLOADS] Error sirviendo {}: {}", filename, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
