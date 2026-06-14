package org.clokey.util;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.requests.*;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.HeadObjectResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.enums.FileExtension;
import org.clokey.enums.ImageType;
import org.clokey.properties.StorageProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageUtil {

    private final ObjectStorageClient objectStorageClient;
    private final StorageProperties storageProperties;

    public PresignedUrlResult createPresignedUrl(
            ImageType imageType, Long memberId, FileExtension fileExtension) {
        String imageKey = UUID.randomUUID().toString();
        String fileName = createFileName(imageType, memberId, imageKey, fileExtension);
        return createPresignedUrlForObject(fileName, imageKey);
    }

    private PresignedUrlResult createPresignedUrlForObject(String fileName, String imageKey) {
        CreatePreauthenticatedRequestDetails details =
                CreatePreauthenticatedRequestDetails.builder()
                        .name("presigned-url-" + imageKey)
                        .objectName(fileName)
                        .accessType(CreatePreauthenticatedRequestDetails.AccessType.ObjectWrite)
                        .timeExpires(getPresignedUrlExpiration())
                        .build();

        CreatePreauthenticatedRequestRequest request =
                CreatePreauthenticatedRequestRequest.builder()
                        .namespaceName(storageProperties.namespace())
                        .bucketName(storageProperties.bucket())
                        .createPreauthenticatedRequestDetails(details)
                        .build();

        CreatePreauthenticatedRequestResponse response =
                objectStorageClient.createPreauthenticatedRequest(request);

        String uploadUrl =
                objectStorageClient.getEndpoint()
                        + response.getPreauthenticatedRequest().getAccessUri();
        String objectUrl = buildPublicObjectUrl(fileName);

        return new PresignedUrlResult(uploadUrl, objectUrl);
    }

    public String buildPublicObjectUrl(String objectKey) {
        return objectStorageClient.getEndpoint()
                + "/n/"
                + storageProperties.namespace()
                + "/b/"
                + storageProperties.bucket()
                + "/o/"
                + encodeObjectKey(objectKey);
    }

    public String toPublicObjectUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        try {
            return buildPublicObjectUrl(extractObjectKey(url));
        } catch (IllegalArgumentException e) {
            log.warn("Failed to normalize object URL, returning original url: {}", url, e);
            return url;
        }
    }

    private String createFileName(
            ImageType imageType, Long memberId, String imageKey, FileExtension fileExtension) {
        return memberId
                + "/"
                + imageType.getType()
                + "/"
                + imageKey
                + "."
                + fileExtension.getExtension();
    }

    public void deleteAllByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            log.info("deleteAllByUrls skipped: received null or empty urls");
            return;
        }

        String namespace = storageProperties.namespace();
        String bucket = storageProperties.bucket();

        for (String url : urls) {
            try {
                String objectKey = extractObjectKey(url);
                DeleteObjectRequest request =
                        DeleteObjectRequest.builder()
                                .namespaceName(namespace)
                                .bucketName(bucket)
                                .objectName(objectKey)
                                .build();
                objectStorageClient.deleteObject(request);
            } catch (Exception e) {
                log.error("Failed to delete object: {}", url, e);
            }
        }
    }

    public void deleteByUrl(String url) {
        if (url == null || url.isBlank()) {
            log.info("deleteByUrl skipped: url is null or blank");
            return;
        }
        try {
            String namespace = storageProperties.namespace();
            String bucket = storageProperties.bucket();
            String objectKey = extractObjectKey(url);

            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .namespaceName(namespace)
                            .bucketName(bucket)
                            .objectName(objectKey)
                            .build();

            objectStorageClient.deleteObject(request);
        } catch (Exception e) {
            log.error("Failed to delete object: {}", url, e);
        }
    }

    private String extractObjectKey(String url) {
        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }

        int oIndex = path.indexOf("/o/");
        if (oIndex == -1) {
            throw new IllegalArgumentException("Invalid URL format: " + url);
        }

        String encodedKey = path.substring(oIndex + 3);
        return URLDecoder.decode(encodedKey, StandardCharsets.UTF_8);
    }

    private String encodeObjectKey(String objectKey) {
        return URLEncoder.encode(objectKey, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private Date getPresignedUrlExpiration() {
        return Date.from(Instant.now().plusSeconds(3600)); // 1시간 후
    }

    public void updateTagToCompleteByUrl(String url) {
        String namespace = storageProperties.namespace();
        String bucket = storageProperties.bucket();
        String objectKey = extractObjectKey(url);

        try {
            HeadObjectRequest headRequest =
                    HeadObjectRequest.builder()
                            .namespaceName(namespace)
                            .bucketName(bucket)
                            .objectName(objectKey)
                            .build();

            HeadObjectResponse headResponse = objectStorageClient.headObject(headRequest);

            Map<String, String> metadata = new HashMap<>();
            if (headResponse.getOpcMeta() != null) {
                metadata.putAll(headResponse.getOpcMeta());
            }

            metadata.put("status", "complete");

            com.oracle.bmc.objectstorage.model.CopyObjectDetails copyDetails =
                    com.oracle.bmc.objectstorage.model.CopyObjectDetails.builder()
                            .sourceObjectName(objectKey)
                            .destinationBucket(bucket)
                            .destinationObjectName(objectKey)
                            .build();

            CopyObjectRequest copyRequest =
                    CopyObjectRequest.builder()
                            .namespaceName(namespace)
                            .bucketName(bucket)
                            .copyObjectDetails(copyDetails)
                            .build();

            objectStorageClient.copyObject(copyRequest);

            log.info(
                    "Object copied for tag update (metadata update may require manual intervention): {}",
                    objectKey);
        } catch (com.oracle.bmc.model.BmcException e) {
            log.error(
                    "Failed to update tag for object: {}, status: {}, message: {}",
                    objectKey,
                    e.getStatusCode(),
                    e.getMessage(),
                    e);
            log.warn("Tag update failed but continuing: {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to update tag for object: {}", objectKey, e);
            log.warn("Tag update failed but continuing: {}", objectKey);
        }
    }

    public void updateTagsToCompleteByUrls(List<String> urls) {
        for (String url : urls) {
            updateTagToCompleteByUrl(url);
        }
    }

    public boolean doesFileExistByUrl(String url) {
        String namespace = storageProperties.namespace();
        String bucket = storageProperties.bucket();
        String objectKey = extractObjectKey(url);

        try {
            HeadObjectRequest request =
                    HeadObjectRequest.builder()
                            .namespaceName(namespace)
                            .bucketName(bucket)
                            .objectName(objectKey)
                            .build();

            HeadObjectResponse response = objectStorageClient.headObject(request);
            return response.get__httpStatusCode__() == 200;
        } catch (com.oracle.bmc.model.BmcException e) {
            if (e.getStatusCode() == 404) {
                return false;
            }
            if (e.getStatusCode() == 403) {
                log.warn("Access denied for key={}, treating as non-existent", objectKey);
                return false;
            }
            log.error("OCI error while checking existence: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Network error while connecting to OCI: {}", e.getMessage());
            return false;
        }
    }

    public boolean doAllFilesExistByUrls(List<String> urls) {
        for (String url : urls) {
            if (!doesFileExistByUrl(url)) {
                log.warn("File not found or inaccessible: {}", url);
                return false;
            }
        }

        return true;
    }
}
