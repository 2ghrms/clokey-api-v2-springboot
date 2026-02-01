package org.clokey.config;

import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import org.clokey.properties.OciProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final OciProperties ociProperties;

    @Bean
    public ObjectStorageClient objectStorageClient() {
        String privateKey = ociProperties.privateKey();
        String passphrase = ociProperties.passphrase();

        SimpleAuthenticationDetailsProvider provider =
                SimpleAuthenticationDetailsProvider.builder()
                        .tenantId(ociProperties.tenancyId())
                        .userId(ociProperties.userId())
                        .fingerprint(ociProperties.fingerprint())
                        .privateKeySupplier(() -> new ByteArrayInputStream(privateKey.getBytes()))
                        .passPhrase(passphrase != null && !passphrase.isEmpty() ? passphrase : null)
                        .region(com.oracle.bmc.Region.fromRegionId(ociProperties.region()))
                        .build();

        return new ObjectStorageClient(provider);
    }
}
