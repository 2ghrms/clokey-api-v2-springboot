package org.clokey.domain.cloth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.response.ClothCreateResponse;
import org.clokey.domain.cloth.service.ClothService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clothes")
@RequiredArgsConstructor
@Tag(name = "3. 옷 API", description = "옷 관련 API입니다.")
@Validated
public class ClothController {

    private final ClothService clothService;

    @PostMapping
    @Operation(summary = "옷 생성", description = "새로운 옷을 생성합니다.")
    public BaseResponse<ClothCreateResponse> createClothes(
            @Valid @RequestBody ClothCreateRequests request) {
        ClothCreateResponse response = clothService.createClothes(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.CREATED, response);
    }
}
