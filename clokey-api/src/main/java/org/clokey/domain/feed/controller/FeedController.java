package org.clokey.domain.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.clokey.code.GlobalBaseSuccessCode;
import org.clokey.domain.feed.dto.response.FeedListResponse;
import org.clokey.domain.feed.query.FollowScope;
import org.clokey.domain.feed.service.FeedService;
import org.clokey.domain.feed.util.FeedRequestParser;
import org.clokey.response.BaseResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feeds")
@RequiredArgsConstructor
@Tag(name = "06. 피드 API", description = "피드 관련 API입니다.")
@Validated
public class FeedController {

    private final FeedService feedService;

    @GetMapping
    @Operation(
            operationId = "Feed_getFeeds",
            summary = "피드 조회(전체/팔로잉)",
            description = "필터링을 통해 피드를 조회합니다.")
    public BaseResponse<FeedListResponse> getFeeds(
            @RequestParam(defaultValue = "ALL") FollowScope followScope,
            @RequestParam(required = false) String styleIds,
            @RequestParam(required = false) String situationIds,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String cursor) {
        List<Long> parsedStyleIds = FeedRequestParser.parseIds(styleIds, 5);
        List<Long> parsedSituationIds = FeedRequestParser.parseIds(situationIds, 5);

        FeedListResponse response =
                feedService.getFeeds(followScope, parsedStyleIds, parsedSituationIds, size, cursor);
        return BaseResponse.onSuccess(GlobalBaseSuccessCode.OK, response);
    }
}
