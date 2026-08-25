package com.flowstudy.core.module.content.storage;

public interface ContentObjectStorage {
    String upload(byte[] bytes, String objectName, String contentType);
}
