package org.clokey.domain.feed.util;

import java.util.Arrays;
import java.util.List;
import org.clokey.exception.BaseCustomException;
import org.clokey.exception.GlobalBaseErrorCode;

public final class FeedRequestParser {

    private FeedRequestParser() {}

    public static List<Long> parseIds(String raw, int maxSize) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        List<Long> ids =
                Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .map(FeedRequestParser::parseLong)
                        .distinct()
                        .toList();

        if (ids.size() > maxSize) {
            throw new BaseCustomException(GlobalBaseErrorCode.BAD_REQUEST);
        }

        return ids;
    }

    public static int parseSize(Integer size, int defaultSize, int maxSize) {
        int resolved = size == null ? defaultSize : size;
        if (resolved < 1 || resolved > maxSize) {
            throw new BaseCustomException(GlobalBaseErrorCode.BAD_REQUEST);
        }
        return resolved;
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BaseCustomException(GlobalBaseErrorCode.BAD_REQUEST);
        }
    }
}
