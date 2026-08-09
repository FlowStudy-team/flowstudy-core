package com.flowstudy.core.module.document.vo;

import com.flowstudy.core.module.document.entity.DocumentFolder;
import java.util.ArrayList;
import java.util.List;

public class DocumentFolderResponse {

    private Long id;
    private String name;
    private Long parentId;
    private String createdAt;
    private String updatedAt;
    private List<DocumentFolderResponse> children;

    public static DocumentFolderResponse from(DocumentFolder folder) {
        DocumentFolderResponse resp = new DocumentFolderResponse();
        resp.id = folder.getId();
        resp.name = folder.getName();
        resp.parentId = folder.getParentId();
        resp.createdAt = folder.getCreatedAt() != null ? folder.getCreatedAt().toString() : null;
        resp.updatedAt = folder.getUpdatedAt() != null ? folder.getUpdatedAt().toString() : null;
        resp.children = new ArrayList<>();
        return resp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<DocumentFolderResponse> getChildren() { return children; }
    public void setChildren(List<DocumentFolderResponse> children) { this.children = children; }
}
