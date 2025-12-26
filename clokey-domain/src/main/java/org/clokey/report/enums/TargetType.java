package org.clokey.report.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TargetType {
    COMMENT("댓글"),
    HISTORY("기록");

    private final String description;

    @JsonCreator
    public static TargetType from(String type) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(type))
                .findFirst()
                .orElse(null);
    }
}
