package org.clokey.domain.term.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.term.dto.request.TermAgreeRequest;
import org.clokey.domain.term.dto.response.MyOptionalTermResponse;
import org.clokey.domain.term.dto.response.TermListResponse;
import org.clokey.domain.term.service.TermService;
import org.clokey.response.BaseResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
@Tag(name = "2. 약관 API", description = "약관 관련 API입니다.")
public class TermController {

    private final TermService termService;

    @GetMapping
    @Operation(summary = "전체 약관 조회", description = "전체 약관 조회 API")
    public BaseResponse<TermListResponse> getTerms() {
        TermListResponse response = termService.getTerms();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PostMapping
    @Operation(summary = "약관 동의", description = "약관 동의 정보를 설정합니다.")
    public BaseResponse<Void> agreeTerm(@Valid @RequestBody TermAgreeRequest request) {
        termService.agreeTerm(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }

    @GetMapping("/my-optional")
    @Operation(summary = "나의 선택 약관 조회", description = "나의 선택 약관 정보 조회 API")
    public BaseResponse<MyOptionalTermResponse> getMyOptionalTerms() {
        MyOptionalTermResponse response = termService.getMyOptionalTerms();
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }

    @PatchMapping("/my-optional-toggle")
    @Operation(summary = "나의 선택 약관 수정", description = "나의 선택 약관 수정 API")
    public BaseResponse<Void> toggleMyOptionalTerms(@RequestParam Long termId) {
        termService.toggleMyOptionalTerms(termId);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, null);
    }
}
