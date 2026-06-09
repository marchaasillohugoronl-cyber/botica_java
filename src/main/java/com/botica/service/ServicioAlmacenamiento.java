package com.botica.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio de almacenamiento de imágenes.
 * - Desarrollo (cloudinary.enabled=false): guarda en disco local ./uploads/
 * - Producción  (cloudinary.enabled=true):  sube a Cloudinary (25GB gratis)
 *
 * imagenPath almacena:
 *   - Local:      nombre de archivo  →  "uuid.jpg"
 *   - Cloudinary: URL completa       →  "https://res.cloudinary.com/..."
 */
@Slf4j
@Service
public class ServicioAlmacenamiento {

    @Value("${app.upload.dir:./uploads/productos}")
    private String directorioSubida;

    @Value("${cloudinary.enabled:false}")
    private boolean cloudinaryHabilitado;

    @Value("${cloudinary.url:}")
    private String cloudinaryUrl;

    private Cloudinary cloudinary;

    @PostConstruct
    public void inicializar() {
        if (cloudinaryHabilitado && cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            cloudinary = new Cloudinary(cloudinaryUrl);
            log.info("Cloudinary habilitado — almacenamiento en la nube activo.");
        } else {
            log.info("Almacenamiento local activo (directorio: {})", directorioSubida);
        }
    }

    /**
     * Guarda la imagen y retorna la referencia para almacenar en BD:
     * - Local:      nombre del archivo ("uuid.jpg")
     * - Cloudinary: URL segura completa ("https://res.cloudinary.com/...")
     */
    public String guardarImagen(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) return null;

        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (!extension.matches("jpg|jpeg|png|gif|webp")) {
            throw new IllegalArgumentException("Formato no permitido: " + extension);
        }

        if (cloudinaryHabilitado && cloudinary != null) {
            return subirACloudinary(archivo);
        }
        return guardarEnLocal(archivo, extension);
    }

    /**
     * Elimina la imagen. Detecta automáticamente si es local o Cloudinary.
     */
    public void eliminarImagen(String referencia) {
        if (referencia == null || referencia.isBlank()) return;

        if (esUrlCloudinary(referencia)) {
            eliminarDeCloudinary(referencia);
        } else {
            eliminarDeLocal(referencia);
        }
    }

    /**
     * Construye la URL pública para mostrar en la vista.
     * - Si ya es una URL completa (Cloudinary): la devuelve tal cual.
     * - Si es un nombre de archivo local: construye la ruta relativa.
     */
    public String obtenerUrlPublica(String referencia) {
        if (referencia == null) return null;
        if (esUrlCloudinary(referencia)) return referencia;
        return "/uploads/" + referencia;
    }

    // ── Cloudinary ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String subirACloudinary(MultipartFile archivo) throws IOException {
        try {
            Map<String, Object> opciones = ObjectUtils.asMap(
                "folder", "botica/productos",
                "overwrite", false,
                "resource_type", "image"
            );
            Map<String, Object> resultado = cloudinary.uploader().upload(archivo.getBytes(), opciones);
            String url = (String) resultado.get("secure_url");
            log.debug("Imagen subida a Cloudinary: {}", url);
            return url;
        } catch (Exception e) {
            throw new IOException("Error al subir imagen a Cloudinary: " + e.getMessage(), e);
        }
    }

    private void eliminarDeCloudinary(String url) {
        try {
            String publicId = extraerPublicId(url);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.debug("Imagen eliminada de Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("No se pudo eliminar imagen de Cloudinary: {}", e.getMessage());
        }
    }

    /** Extrae el public_id de una URL de Cloudinary para poder eliminarla. */
    private String extraerPublicId(String url) {
        // URL ejemplo: https://res.cloudinary.com/cloud/image/upload/v123/botica/productos/abc.jpg
        // public_id:   botica/productos/abc
        String path = url.replaceFirst("https://res\\.cloudinary\\.com/[^/]+/image/upload/", "");
        path = path.replaceFirst("^v\\d+/", "");
        return path.replaceAll("\\.[^.]+$", "");
    }

    private boolean esUrlCloudinary(String referencia) {
        return referencia.startsWith("https://res.cloudinary.com/");
    }

    // ── Almacenamiento local ─────────────────────────────────────────────────

    private String guardarEnLocal(MultipartFile archivo, String extension) throws IOException {
        Path rutaSubida = Paths.get(directorioSubida);
        if (!Files.exists(rutaSubida)) {
            Files.createDirectories(rutaSubida);
        }
        String nombreArchivo = UUID.randomUUID() + "." + extension;
        Path destino = rutaSubida.resolve(nombreArchivo);
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return nombreArchivo;
    }

    private void eliminarDeLocal(String nombreArchivo) {
        try {
            Path ruta = Paths.get(directorioSubida).resolve(nombreArchivo);
            Files.deleteIfExists(ruta);
        } catch (IOException ignorado) {}
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) return "jpg";
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
    }
}
