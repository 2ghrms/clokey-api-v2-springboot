package org.clokey.domain.member.event;

public record NewPendingFollowerEvent(Long followFromId, Long followToId) {}
