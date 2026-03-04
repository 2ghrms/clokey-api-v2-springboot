package org.clokey;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestObjectStorageConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectStorageClient.class)
    public ObjectStorageClient objectStorageClient() {
        return org.mockito.Mockito.mock(ObjectStorageClient.class);
    }
}
