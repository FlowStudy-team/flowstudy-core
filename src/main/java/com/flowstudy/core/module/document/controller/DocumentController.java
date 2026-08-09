package com.flowstudy.core.module.document.controller;

import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.common.result.Result;
import com.flowstudy.core.module.document.dto.CreateDocumentFolderRequest;
import com.flowstudy.core.module.document.dto.CreateDocumentRequest;
import com.flowstudy.core.module.document.dto.UpdateDocumentRequest;
import com.flowstudy.core.module.document.service.DocumentService;
import com.flowstudy.core.module.document.vo.DocumentCategoryResponse;
import com.flowstudy.core.module.document.vo.DocumentDetailResponse;
import com.flowstudy.core.module.document.vo.DocumentFolderResponse;
import com.flowstudy.core.module.document.vo.DocumentItemResponse;
import com.flowstudy.core.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public Result<PageResponse<DocumentItemResponse>> listDocuments(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        log.info("GET /documents userId={} keyword={} folderId={} status={} page={} pageSize={}",
                user.id(), keyword, folderId, status, page, pageSize);
        return Result.success(documentService.listDocuments(
                user.id(), keyword, folderId, categoryId, tag, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<DocumentDetailResponse> getDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        log.info("GET /documents/{} userId={}", id, user.id());
        return Result.success(documentService.getDocument(user.id(), id));
    }

    @PostMapping
    public Result<DocumentDetailResponse> createDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateDocumentRequest request) {
        log.info("POST /documents userId={} title={} folderId={} contentLen={}",
                user.id(), request.title(), request.folderId(),
                request.content() != null ? request.content().length() : 0);
        return Result.success(documentService.createDocument(user.id(), request));
    }

    @PutMapping("/{id}")
    public Result<DocumentDetailResponse> updateDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateDocumentRequest request) {
        log.info("PUT /documents/{} userId={} title={} status={}", id, user.id(), request.title(), request.status());
        return Result.success(documentService.updateDocument(user.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        log.info("DELETE /documents/{} userId={}", id, user.id());
        documentService.deleteDocument(user.id(), id);
        return Result.success(null);
    }

    @GetMapping("/categories")
    public Result<List<DocumentCategoryResponse>> getCategories() {
        log.info("GET /documents/categories");
        return Result.success(documentService.getCategories());
    }

    @GetMapping("/folders")
    public Result<List<DocumentFolderResponse>> getFolders(
            @AuthenticationPrincipal AuthenticatedUser user) {
        log.info("GET /documents/folders userId={}", user.id());
        return Result.success(documentService.getFolders(user.id()));
    }

    @PostMapping("/folders")
    public Result<DocumentFolderResponse> createFolder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateDocumentFolderRequest request) {
        log.info("POST /documents/folders userId={} name={} parentId={}", user.id(), request.name(), request.parentId());
        return Result.success(documentService.createFolder(user.id(), request));
    }

    @DeleteMapping("/folders/{id}")
    public Result<Void> deleteFolder(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long id) {
        log.info("DELETE /documents/folders/{} userId={}", id, user.id());
        documentService.deleteFolder(user.id(), id);
        return Result.success(null);
    }
}
