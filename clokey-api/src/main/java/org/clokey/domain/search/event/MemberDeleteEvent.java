package org.clokey.domain.search.event;

import java.util.List;

public record MemberDeleteEvent(Long memberId, List<Long> historyIds) {
    public static MemberDeleteEvent of(Long memberId, List<Long> historyIds) {
        return new MemberDeleteEvent(memberId, historyIds);
    }
}
