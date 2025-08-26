package org.clokey.domain.term.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.term.dto.TermAgreeRequest;
import org.clokey.domain.term.service.TermService;
import org.clokey.response.BaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/terms")
@RequiredArgsConstructor
@Tag(name = "2. 약관 API", description = "약관 관련 API입니다.")
public class TermController {

    private final TermService termService;

    @PostMapping
    @Operation(summary = "약관 동의", description = "약관 동의 정보를 설정합니다.")
    public BaseResponse<Void> agreeTerm(@Valid @RequestBody TermAgreeRequest request) {
        termService.agreeTerm(request);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }
}
