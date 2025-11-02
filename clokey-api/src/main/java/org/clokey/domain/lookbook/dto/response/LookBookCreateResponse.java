package org.clokey.domain.lookbook.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.clokey.lookbook.entity.LookBook;

public record LookBookCreateResponse(
        @Schema(description = "생성된 룩북 ID", example = "1") Long lookBookId) {
    public static LookBookCreateResponse from(LookBook lookBook) {
        return new LookBookCreateResponse(lookBook.getId());
    }
}
