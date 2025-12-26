package org.clokey.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileExtension {
    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    WEBP("webp"),
    HEIC("heic"),
    HEIF("heif");

    private final String extension;

    @JsonCreator
    public static FileExtension from(String extension) {
        return Stream.of(values())
                .filter(e -> e.name().equalsIgnoreCase(extension))
                .findFirst()
                .orElse(null);
    }
}
