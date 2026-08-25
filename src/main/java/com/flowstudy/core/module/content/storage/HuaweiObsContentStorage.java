package com.flowstudy.core.module.content.storage;

import com.obs.services.ObsClient;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.PutObjectResult;
import java.io.ByteArrayInputStream;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flowstudy.obs.enabled", havingValue = "true")
public class HuaweiObsContentStorage implements ContentObjectStorage {
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;

    public HuaweiObsContentStorage(
            @Value("${flowstudy.obs.endpoint}") String endpoint,
            @Value("${flowstudy.obs.access-key}") String accessKey,
            @Value("${flowstudy.obs.secret-key}") String secretKey,
            @Value("${flowstudy.obs.bucket-name}") String bucketName) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
    }

    @Override
    public String upload(byte[] bytes, String objectName, String contentType) {
        Objects.requireNonNull(bytes, "bytes");
        try (ObsClient client = new ObsClient(accessKey, secretKey, endpoint)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            PutObjectResult result = client.putObject(
                    bucketName, objectName, new ByteArrayInputStream(bytes), metadata);
            if (result == null) throw new IllegalStateException("OBS upload returned no result");
            String normalizedEndpoint = endpoint.replaceFirst("^https?://", "");
            return "https://" + bucketName + "." + normalizedEndpoint + "/" + objectName;
        } catch (Exception exception) {
            throw new IllegalStateException("content asset upload failed", exception);
        }
    }
}
