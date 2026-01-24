package org.clokey.domain.search.document;

import io.vanslog.spring.data.meilisearch.annotations.Document;
import io.vanslog.spring.data.meilisearch.annotations.Setting;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
@Document(indexUid = "members")
@Setting(
        searchableAttributes = {"nickname"},
        displayedAttributes = {"id", "memberStatus", "profileImageUrl", "nickname"},
        filterableAttributes = {"id", "memberStatus"},
        rankingRules = {"typo", "words", "proximity", "attribute", "exactness"})
public class MemberDocument {

    @Id private String id; // Member.id.toString()

    private String memberStatus; // Member.memberStatus

    private String profileImageUrl; // Member.profileUrl

    private String nickname; // Member.nickname
}
