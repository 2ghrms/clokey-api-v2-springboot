package org.clokey.domain.term.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.clokey.term.entity.MemberTerm;

public record MyOptionalTermResponse(@Schema(description = "나의 선택 약관 정보") List<Payload> payloads) {
    public record Payload(
            @Schema(description = "약관 ID") Long termId,
            @Schema(description = "동의 여부") boolean agreed) {}

    public static MyOptionalTermResponse from(List<MemberTerm> memberTerms) {
        List<MyOptionalTermResponse.Payload> payloads =
                memberTerms.stream()
                        .map(
                                term ->
                                        new MyOptionalTermResponse.Payload(
                                                term.getTerm().getId(), term.isAgreed()))
                        .toList();

        return new MyOptionalTermResponse(payloads);
    }
}
