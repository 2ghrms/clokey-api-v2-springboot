package org.clokey.util;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.clokey.properties.WebClientProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientUtil {

    private final WebClient.Builder webClientBuilder;
    private final WebClientProperties webClientProperties;

    public <T, R> Mono<R> postToAiServer(String path, T requestBody, Class<R> responseType) {
        final long startedAtNs = System.nanoTime();
        final String aiServerIp = webClientProperties.aiServerIp();
        WebClient webClient = webClientBuilder.baseUrl("http://" + aiServerIp).build();

        return webClient
                .post()
                .uri(path)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .doOnError(
                        error ->
                                logAiCallFailure(
                                        path,
                                        aiServerIp,
                                        responseType.getSimpleName(),
                                        elapsedMillis(startedAtNs),
                                        error));
    }

    private void logAiCallFailure(
            String path, String aiServerIp, String responseType, long elapsedMs, Throwable error) {
        if (error instanceof WebClientResponseException webClientResponseException) {
            log.error(
                    "[web-client] AI 서버 응답 오류 - path: {}, aiServerIp: {}, responseType: {}, status: {}, elapsedMs: {}, message: {}",
                    path,
                    aiServerIp,
                    responseType,
                    webClientResponseException.getStatusCode().value(),
                    elapsedMs,
                    webClientResponseException.getMessage(),
                    error);
            return;
        }

        if (error instanceof WebClientRequestException) {
            log.error(
                    "[web-client] AI 서버 요청/연결 실패 - path: {}, aiServerIp: {}, responseType: {}, elapsedMs: {}, message: {}",
                    path,
                    aiServerIp,
                    responseType,
                    elapsedMs,
                    error.getMessage(),
                    error);
            return;
        }

        log.error(
                "[web-client] AI 서버 예상치 못한 오류 - path: {}, aiServerIp: {}, responseType: {}, elapsedMs: {}, message: {}",
                path,
                aiServerIp,
                responseType,
                elapsedMs,
                error.getMessage(),
                error);
    }

    private long elapsedMillis(long startedAtNs) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAtNs);
    }
}
