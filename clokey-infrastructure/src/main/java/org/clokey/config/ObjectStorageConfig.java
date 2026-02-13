package org.clokey.config;

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.clokey.properties.OciProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ObjectStorageConfig {

    private final OciProperties ociProperties;

    /** 환경 변수에 PEM 키가 "\\n" 문자열로 들어온 경우 실제 줄바꿈으로 복원 */
    private static String normalizePemPrivateKey(String privateKey) {
        if (privateKey == null || privateKey.isBlank()) {
            return privateKey;
        }
        return privateKey.replace("\\n", "\n");
    }

    @Bean
    public ObjectStorageClient objectStorageClient() {
        String rawKey = ociProperties.privateKey();
        String privateKey = normalizePemPrivateKey(rawKey);
        String passphrase = ociProperties.passphrase();

        SimpleAuthenticationDetailsProvider provider =
                SimpleAuthenticationDetailsProvider.builder()
                        .tenantId(ociProperties.tenancyId())
                        .userId(ociProperties.userId())
                        .fingerprint(ociProperties.fingerprint())
                        .privateKeySupplier(
                                () ->
                                        new ByteArrayInputStream(
                                                privateKey.getBytes(StandardCharsets.UTF_8)))
                        .passPhrase(passphrase != null && !passphrase.isEmpty() ? passphrase : null)
                        .region(com.oracle.bmc.Region.fromRegionId(ociProperties.region()))
                        .build();

        return new ObjectStorageClient(provider);
    }
}
