package org.clokey.config;

import io.vanslog.spring.data.meilisearch.client.ClientConfiguration;
import io.vanslog.spring.data.meilisearch.config.MeilisearchConfiguration;
import io.vanslog.spring.data.meilisearch.repository.config.EnableMeilisearchRepositories;
import lombok.RequiredArgsConstructor;
import org.clokey.properties.MeilisearchProperties;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@EnableMeilisearchRepositories(basePackages = {"org.clokey.domain.search.repository"})
@Profile("!test")
public class MeiliSearchConfig extends MeilisearchConfiguration {

    private final MeilisearchProperties meilisearchProperties;

    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(meilisearchProperties.url())
                .withApiKey(meilisearchProperties.apiKey())
                .build();
    }
}
