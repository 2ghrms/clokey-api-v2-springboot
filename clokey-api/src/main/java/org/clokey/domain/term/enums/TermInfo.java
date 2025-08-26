package org.clokey.domain.term.enums;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TermInfo {
    SERVICE_USE(1L, false),
    PRIVATE_POLICY(2L, false),
    LOCATION_BASED_SERVICE(3L, false),
    MARKETING_INFO_RECEIVE(4L, true),
    PUSH_NOTIFICATION_RECEIVE(5L, true);

    private Long id;
    private boolean optional;

    public static List<Long> getNonOptionalIds() {
        return Arrays.stream(values())
                .filter(term -> !term.isOptional())
                .map(TermInfo::getId)
                .toList();
    }

    public static List<Long> getAllIds() {
        return Arrays.stream(values()).map(TermInfo::getId).toList();
    }
}
