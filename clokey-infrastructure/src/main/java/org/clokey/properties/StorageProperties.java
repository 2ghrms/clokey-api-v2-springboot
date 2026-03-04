package org.clokey.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oci.objectstorage")
public record StorageProperties(String namespace, String bucket) {}
