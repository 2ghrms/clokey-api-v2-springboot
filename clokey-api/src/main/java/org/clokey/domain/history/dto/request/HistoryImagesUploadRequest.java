package org.clokey.domain.history.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.clokey.enums.FileExtension;
import org.clokey.global.annotation.Enum;

public record HistoryImagesUploadRequest(
        @NotEmpty(message = "업로드할 파일들의 정보가 비어있을 수 없습니다.") @Valid @Schema(description = "업로드 요청 리스트")
                List<Payload> payloads) {
    @Schema(name = "HistoryImagesUploadRequestPayload")
    public record Payload(
            @Enum(message = "파일 확장자는 비어있을 수 없으며 PNG, JPG, JPEG, WEBP, HEIC, HEIF만 가능합니다.")
                    @Schema(description = "파일 확장자", defaultValue = "JPEG")
                    FileExtension fileExtension,
            @NotBlank(message = "MD5 해시값이 비어있을 수 없습니다.")
                    @Schema(description = "S3 업로드시 파일의 무결성을 확인하기 위한 md5 해시")
                    String md5Hashes) {}
}
