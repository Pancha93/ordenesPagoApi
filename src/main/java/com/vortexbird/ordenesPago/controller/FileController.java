package com.vortexbird.ordenesPago.controller;

import com.vortexbird.ordenesPago.service.PresignedUrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controlador para servir archivos almacenados localmente.
 * Simula el comportamiento de AWS S3 con presigned URLs.
 * 
 * NO requiere autenticación JWT porque los tokens presigned ya proporcionan
 * su propia seguridad (son temporales y expiran en 15 minutos).
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Files", description = "Descarga de archivos con presigned URLs (simula S3)")
public class FileController {
    
    private final PresignedUrlService presignedUrlService;
    
    @Value("${storage.local.base-path:./uploads}")
    private String basePath;
    
    /**
     * Obtiene un archivo usando un token temporal (presigned URL).
     * Similar a AWS S3 GetObject con presigned URL.
     * 
     * @param token Token temporal (UUID) que viene en el campo 'fileUrl' de InvoiceResponse.
     *              Ejemplo: Si fileUrl es "http://localhost:8080/api/files/550e8400-e29b-41d4-a716-446655440000"
     *              entonces token = "550e8400-e29b-41d4-a716-446655440000"
     *              
     *              El token expira después de 15 minutos por defecto.
     */
    @GetMapping("/{token}")
    @Operation(
        summary = "Obtener archivo con presigned token", 
        description = "Obtiene un archivo usando un token temporal generado automáticamente. " +
                      "El token viene en el campo 'fileUrl' de la respuesta cuando consultas una factura. " +
                      "Similar a AWS S3 presigned URLs - el token expira después de 15 minutos. " +
                      "El archivo se visualiza inline en el navegador (para PDFs e imágenes)."
    )
    public ResponseEntity<Resource> getFile(
            @Parameter(description = "Token temporal (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String token) {
        try {
            // Validar token y obtener la clave del archivo
            String storageKey = presignedUrlService.validateAndGetStorageKey(token);
            log.info("Serving file with token: {} -> {}", token, storageKey);
            
            // Obtener el path del archivo
            Path filePath = Paths.get(basePath, storageKey).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", storageKey);
                return ResponseEntity.notFound().build();
            }
            
            // Determinar el tipo de contenido
            String contentType = determineContentType(filePath);
            
            // Obtener el nombre del archivo original
            String filename = extractOriginalFilename(filePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            log.error("Invalid or expired token: {}", token);
            return ResponseEntity.status(403).body(null);
        } catch (MalformedURLException e) {
            log.error("Invalid file path", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error serving file", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Fuerza la descarga de un archivo (attachment) en lugar de visualizarlo inline.
     * Similar a S3 presigned URL con response-content-disposition=attachment.
     */
    @GetMapping("/{token}/download")
    @Operation(
        summary = "Descargar archivo con presigned token", 
        description = "Descarga un archivo (fuerza descarga en lugar de visualización inline). " +
                      "Agrega '/download' al final de la URL del token para forzar la descarga. " +
                      "Ejemplo: si fileUrl es 'http://.../api/files/abc123', " +
                      "usa 'http://.../api/files/abc123/download'"
    )
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Token temporal (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String token) {
        try {
            // Validar token y obtener la clave del archivo
            String storageKey = presignedUrlService.validateAndGetStorageKey(token);
            log.info("Downloading file with token: {} -> {}", token, storageKey);
            
            // Obtener el path del archivo
            Path filePath = Paths.get(basePath, storageKey).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", storageKey);
                return ResponseEntity.notFound().build();
            }
            
            // Determinar el tipo de contenido
            String contentType = determineContentType(filePath);
            
            // Obtener el nombre del archivo original
            String filename = extractOriginalFilename(filePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            log.error("Invalid or expired token: {}", token);
            return ResponseEntity.status(403).body(null);
        } catch (MalformedURLException e) {
            log.error("Invalid file path", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error downloading file", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Determina el tipo de contenido del archivo
     */
    private String determineContentType(Path filePath) {
        try {
            String contentType = Files.probeContentType(filePath);
            if (contentType != null) {
                return contentType;
            }
        } catch (IOException e) {
            log.warn("Could not determine file type", e);
        }
        
        // Fallback basado en la extensión
        String filename = filePath.getFileName().toString().toLowerCase();
        if (filename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        }
        
        return "application/octet-stream";
    }
    
    /**
     * Extrae el nombre original del archivo (sin el UUID prefijo)
     */
    private String extractOriginalFilename(Path filePath) {
        String filename = filePath.getFileName().toString();
        // Los archivos guardados tienen formato: UUID_nombreOriginal.ext
        // Remover el UUID del principio si existe
        if (filename.contains("_")) {
            return filename.substring(filename.indexOf("_") + 1);
        }
        return filename;
    }
}
