package com.flowstudy.core.module.content.controller;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.content.storage.ContentObjectStorage;
import com.flowstudy.core.security.AuthenticatedUser;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/content/assets")
public class ContentAssetController {
    private static final long MAX_BYTES = 10 * 1024 * 1024;
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final ObjectProvider<ContentObjectStorage> storageProvider;

    public ContentAssetController(ObjectProvider<ContentObjectStorage> storageProvider) {
        this.storageProvider = storageProvider;
    }

    @PostMapping(consumes = "multipart/form-data")
    public Result<String> upload(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("file") MultipartFile file) {
        ContentObjectStorage storage = storageProvider.getIfAvailable();
        if (storage == null) {
            throw new BusinessException(45001, "content object storage is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new BusinessException(45002, "image must be between 1 byte and 10 MB", HttpStatus.BAD_REQUEST);
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException(45003, "only jpeg, png, webp and gif images are supported", HttpStatus.BAD_REQUEST);
        }
        String originalName = file.getOriginalFilename() == null ? "asset" : file.getOriginalFilename();
        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.')).toLowerCase(Locale.ROOT) : "";
        String objectName = "content/" + user.id() + "/" + UUID.randomUUID() + extension;
        try {
            return Result.success(storage.upload(file.getBytes(), objectName, contentType));
        } catch (java.io.IOException exception) {
            throw new BusinessException(45004, "unable to read uploaded image", HttpStatus.BAD_REQUEST);
        }
    }
}
