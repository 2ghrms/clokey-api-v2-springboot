package org.clokey.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.properties.WebClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientUtil {

    private final WebClient.Builder webClientBuilder;
    private final WebClientProperties webClientProperties;

    public <T, R> Mono<R> postToAiServer(String path, T requestBody, Class<R> responseType) {
        WebClient webClient =
                webClientBuilder.baseUrl("http://" + webClientProperties.aiServerIp()).build();

        return webClient
                .post()
                .uri(path)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(error -> log.error("AI 서버 요청 실패: {}", error.getMessage(), error));
    }
}
