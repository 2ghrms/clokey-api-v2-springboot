package org.clokey.domain.member.event;

public record NewFollowerEvent(Long followFromId, Long followToId) {}
