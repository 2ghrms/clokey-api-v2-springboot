package org.clokey.domain.feed.controller;

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
@RequiredArgsConstructor
@Validated
@RequestMapping("/feeds")
public class FeedController {

    private final FeedService feedService;

    @GetMapping
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
