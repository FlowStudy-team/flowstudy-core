package com.flowstudy.core.module.document.service;

import com.flowstudy.core.common.exception.BusinessException;
import com.flowstudy.core.common.result.PageResponse;
import com.flowstudy.core.module.document.dto.CreateDocumentFolderRequest;
import com.flowstudy.core.module.document.dto.CreateDocumentRequest;
import com.flowstudy.core.module.document.dto.UpdateDocumentRequest;
import com.flowstudy.core.module.document.entity.Document;
import com.flowstudy.core.module.document.entity.DocumentCategory;
import com.flowstudy.core.module.document.entity.DocumentFolder;
import com.flowstudy.core.module.document.mapper.DocumentMapper;
import com.flowstudy.core.module.document.vo.DocumentCategoryResponse;
import com.flowstudy.core.module.document.vo.DocumentDetailResponse;
import com.flowstudy.core.module.document.vo.DocumentFolderResponse;
import com.flowstudy.core.module.document.vo.DocumentItemResponse;
import com.flowstudy.core.module.content.ContentStatusMachine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final DocumentMapper documentMapper;

    public DocumentService(DocumentMapper documentMapper) {
        this.documentMapper = documentMapper;
        log.info("DocumentService initialized");
    }

    // ---- Document CRUD ----

    public PageResponse<DocumentItemResponse> listDocuments(
            Long userId,
            String keyword,
            Long folderId,
            Long categoryId,
            String tag,
            String status,
            Integer page,
            Integer size) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 24 : Math.min(size, MAX_PAGE_SIZE);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        String normalizedTag = tag == null || tag.isBlank() ? null : tag.trim();
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim();

        long total = documentMapper.countByUser(userId, normalizedKeyword, folderId, categoryId, normalizedTag, normalizedStatus);
        List<DocumentItemResponse> records = documentMapper.findPageByUser(
                        userId, normalizedKeyword, folderId, categoryId, normalizedTag, normalizedStatus,
                        safeSize, (safePage - 1) * safeSize)
                .stream()
                .map(DocumentItemResponse::from)
                .toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    public DocumentDetailResponse getDocument(Long userId, Long documentId) {
        Document doc = documentMapper.findByIdAndUser(documentId, userId);
        if (doc == null) {
            throw new BusinessException(43001, "document does not exist", HttpStatus.NOT_FOUND);
        }
        return DocumentDetailResponse.from(doc);
    }

    public DocumentDetailResponse createDocument(Long userId, CreateDocumentRequest request) {
        log.info("createDocument: userId={} title={} folderId={} contentLen={}",
                userId, request.title(), request.folderId(),
                request.content() != null ? request.content().length() : 0);
        Document doc = new Document();
        doc.setUserId(userId);
        doc.setTitle(request.title().trim());
        doc.setContent(request.content());
        doc.setFolderId(request.folderId());
        doc.setCategoryId(request.categoryId());
        doc.setTags(request.tags() != null && !request.tags().isEmpty()
                ? String.join(",", request.tags()) : null);
        doc.setStatus("draft");
        log.info("createDocument: inserting doc into DB...");
        documentMapper.insert(doc);
        log.info("createDocument: inserted doc id={}", doc.getId());
        DocumentDetailResponse detail = getDocument(userId, doc.getId());
        log.info("createDocument: returning detail id={} title={}", detail.id(), detail.title());
        return detail;
    }

    public DocumentDetailResponse updateDocument(Long userId, Long documentId, UpdateDocumentRequest request) {
        Document doc = documentMapper.findByIdAndUser(documentId, userId);
        if (doc == null) {
            throw new BusinessException(43001, "document does not exist", HttpStatus.NOT_FOUND);
        }
        if (request.title() != null) doc.setTitle(request.title().trim());
        if (request.content() != null) doc.setContent(request.content());
        if (request.summary() != null) doc.setSummary(request.summary().trim());
        if (request.folderId() != null) doc.setFolderId(request.folderId());
        if (request.categoryId() != null) doc.setCategoryId(request.categoryId());
        if (request.tags() != null) doc.setTags(String.join(",", request.tags()));
        if (request.status() != null) {
            String normalized = ContentStatusMachine.transition(doc.getStatus(), request.status());
            doc.setStatus(normalized.toLowerCase(java.util.Locale.ROOT));
        }
        documentMapper.update(doc);
        return getDocument(userId, documentId);
    }

    public void deleteDocument(Long userId, Long documentId) {
        int affected = documentMapper.softDelete(documentId, userId);
        if (affected == 0) {
            throw new BusinessException(43001, "document does not exist", HttpStatus.NOT_FOUND);
        }
    }

    // ---- Categories ----

    public List<DocumentCategoryResponse> getCategories() {
        List<DocumentCategory> categories = documentMapper.findAllCategories();
        return categories.stream()
                .map(cat -> new DocumentCategoryResponse(cat.getId(), cat.getName()))
                .toList();
    }

    // ---- Folders ----

    public List<DocumentFolderResponse> getFolders(Long userId) {
        log.info("getFolders: userId={}", userId);
        List<DocumentFolder> all = documentMapper.findFoldersByUser(userId);
        log.info("getFolders: found {} folders", all.size());
        Map<Long, List<DocumentFolderResponse>> childrenMap = new HashMap<>();
        List<DocumentFolderResponse> roots = new ArrayList<>();

        for (DocumentFolder folder : all) {
            DocumentFolderResponse resp = DocumentFolderResponse.from(folder);
            childrenMap.put(folder.getId(), resp.getChildren());
            if (folder.getParentId() == null) {
                roots.add(resp);
            }
        }

        for (DocumentFolder folder : all) {
            if (folder.getParentId() != null) {
                List<DocumentFolderResponse> siblings = childrenMap.get(folder.getParentId());
                if (siblings != null) {
                    siblings.add(DocumentFolderResponse.from(folder));
                }
            }
        }

        return roots;
    }

    public DocumentFolderResponse createFolder(Long userId, CreateDocumentFolderRequest request) {
        log.info("createFolder: userId={} name={} parentId={}", userId, request.name(), request.parentId());
        DocumentFolder folder = new DocumentFolder();
        folder.setUserId(userId);
        folder.setName(request.name().trim());
        folder.setParentId(request.parentId());
        documentMapper.insertFolder(folder);
        log.info("createFolder: inserted folder id={}", folder.getId());
        return DocumentFolderResponse.from(folder);
    }

    public void deleteFolder(Long userId, Long folderId) {
        int affected = documentMapper.softDeleteFolder(folderId, userId);
        if (affected == 0) {
            throw new BusinessException(43002, "folder does not exist", HttpStatus.NOT_FOUND);
        }
    }
}
