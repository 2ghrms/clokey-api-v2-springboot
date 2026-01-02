package org.clokey.domain.like.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.like.service.LikeService;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Tag(name = "09. 좋아요 API", description = "좋아요 관련 API입니다.")
@Validated
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @Operation(operationId = "Like_toggleLike", summary = "좋아요 생성", description = "기록에 좋아요를 추가합니다")
    public BaseResponse<Void> toggleLike(@RequestParam("historyId") Long historyId) {

        likeService.toggleLike(historyId);

        return BaseResponse.onSuccess(GlobalBaseSuccessCode.NO_CONTENT, null);
    }
}
