package org.clokey.domain.cloth.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ClothCreateRequests(
        @NotEmpty(message = "적어도 하나의 옷을 생성해야 합니다.") @Valid List<ClothCreateRequest> content) {}
