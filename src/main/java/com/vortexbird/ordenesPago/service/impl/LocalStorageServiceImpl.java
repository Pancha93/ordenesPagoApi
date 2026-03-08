package com.vortexbird.ordenesPago.service.impl;

import com.vortexbird.ordenesPago.exception.StorageException;
import com.vortexbird.ordenesPago.service.PresignedUrlService;
import com.vortexbird.ordenesPago.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementación local de StorageService para desarrollo.
 * Almacena archivos en el sistema de archivos local y simula el comportamiento de S3.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements StorageService {
    
    private final PresignedUrlService presignedUrlService;
    
    @Value("${storage.local.base-path:./uploads}")
    private String basePath;
    
    @Override
    public String store(MultipartFile file, String folder) {
        try {
            // Crear directorio si no existe
            Path folderPath = Paths.get(basePath, folder);
            Files.createDirectories(folderPath);
            
            // Generar nombre único
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = folderPath.resolve(filename);
            
            // Guardar archivo
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            String storageKey = folder + "/" + filename;
            log.info("File stored successfully: {}", storageKey);
            
            return storageKey;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new StorageException("No se pudo almacenar el archivo", e);
        }
    }
    
    @Override
    public String generateDownloadUrl(String storageKey, int expirationMinutes) {
        // Generar token presigned temporal (simula S3 presigned URLs)
        String token = presignedUrlService.generatePresignedToken(storageKey, expirationMinutes);
        return "http://localhost:8080/api/files/" + token;
    }
    
    @Override
    public void delete(String storageKey) {
        try {
            Path filePath = Paths.get(basePath, storageKey);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", storageKey);
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storageKey, e);
            throw new StorageException("No se pudo eliminar el archivo", e);
        }
    }
    
    @Override
    public boolean exists(String storageKey) {
        Path filePath = Paths.get(basePath, storageKey);
        return Files.exists(filePath);
    }
}
