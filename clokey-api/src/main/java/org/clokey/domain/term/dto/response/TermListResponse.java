package org.clokey.domain.term.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.clokey.term.entity.Term;

public record TermListResponse(
        @Schema(description = "약관 정보들의 리스트") List<TermListResponse.Payload> payloads) {
    public record Payload(
            @Schema(description = "약관 ID") Long termId,
            @Schema(description = "약관 제목") String title,
            @Schema(description = "약관 내용") String body,
            @Schema(description = "필수 동의 여부") boolean optional) {}

    public static TermListResponse of(List<Term> terms) {
        List<TermListResponse.Payload> payloads =
                terms.stream()
                        .map(
                                term ->
                                        new TermListResponse.Payload(
                                                term.getId(),
                                                term.getTitle(),
                                                term.getBody(),
                                                term.isOptional()))
                        .toList();

        return new TermListResponse(payloads);
    }
}
