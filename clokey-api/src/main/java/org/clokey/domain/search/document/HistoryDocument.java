package org.clokey.domain.search.document;

import io.vanslog.spring.data.meilisearch.annotations.Document;
import io.vanslog.spring.data.meilisearch.annotations.Setting;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Document(indexUid = "histories")
@Setting(
        searchableAttributes = {"styleNames", "hashtagNames", "categoryNames"},
        displayedAttributes = {
            "id",
            "memberId",
            "banned",
            "likeCount",
            "createdAt",
            "historyImageUrl",
            "profileImageUrl",
            "nickname",
            "styleNames",
            "hashtagNames",
            "categoryNames"
        },
        sortableAttributes = {"likeCount", "createdAt"},
        filterableAttributes = {"memberId", "banned"},
        rankingRules = {"typo", "words", "proximity", "attribute", "sort", "exactness"})
public class HistoryDocument {

    @Id private String id; // History.id.toString()

    private Long memberId; // History.member.id

    private Boolean banned; // History.banned

    private Long likeCount; // MemberLike count

    private Long createdAt; // History.createdAt, epoch millis (UTC)

    private String historyImageUrl; // History.imageUrl

    private String profileImageUrl; // Member.profileUrl

    private String nickname; // Member.nickname

    private List<String> styleNames;

    private List<String> hashtagNames;

    private List<String> categoryNames;
}
