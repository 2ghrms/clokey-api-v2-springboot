package org.clokey.util;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.enums.FileExtension;
import org.clokey.enums.ImageType;
import org.clokey.properties.S3Properties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3Util {

    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;

    public String createPresignedUrl(
            ImageType imageType, Long targetId, FileExtension fileExtension, String md5Hash) {
        String imageKey = UUID.randomUUID().toString();
        String fileName = createFileName(imageType, targetId, imageKey, fileExtension);
        String bucket = s3Properties.bucket();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                generatePresignedUrlRequest(
                        bucket, fileName, fileExtension.getExtension(), md5Hash);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
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

    private GeneratePresignedUrlRequest generatePresignedUrlRequest(
            String bucket, String fileName, String imageFileExtension, String md5Hash) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName, HttpMethod.PUT)
                        .withKey(fileName)
                        .withContentType("image/" + imageFileExtension)
                        .withExpiration(getPresignedUrlExpiration());

        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL, CannedAccessControlList.PublicRead.toString());

        generatePresignedUrlRequest.setContentMd5(md5Hash);

        return generatePresignedUrlRequest;
    }

    public void deleteAllByUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            log.info("deleteAllByUrls skipped: received null or empty urls");
            return;
        }
        String bucket = s3Properties.bucket();

        List<DeleteObjectsRequest.KeyVersion> keys =
                urls.stream()
                        .map(this::extractObjectKey)
                        .map(DeleteObjectsRequest.KeyVersion::new)
                        .toList();

        DeleteObjectsRequest request = new DeleteObjectsRequest(bucket).withKeys(keys);
        amazonS3.deleteObjects(request);
    }

    public void deleteByUrl(String url) {
        String bucket = s3Properties.bucket();
        String objectKey = extractObjectKey(url);
        amazonS3.deleteObject(bucket, objectKey);
    }

    private String extractObjectKey(String url) {
        int comIndex = url.indexOf(".com/");
        return url.substring(comIndex + 5);
    }

    private Date getPresignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += TimeUnit.MINUTES.toMillis(1);
        expiration.setTime(expTimeMillis);

        return expiration;
    }
}
