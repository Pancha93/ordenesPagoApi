package com.vortexbird.ordenesPago.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String folder);
    String generateDownloadUrl(String storageKey, int expirationMinutes);
    void delete(String storageKey);
    boolean exists(String storageKey);
}
