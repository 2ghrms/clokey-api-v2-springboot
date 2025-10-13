package org.clokey.domain.image.event;

import java.util.List;

public record ImagesDeleteEvent(List<String> imageUrls) {
    public static ImagesDeleteEvent of(List<String> imageUrls) {
        return new ImagesDeleteEvent(imageUrls);
    }
}
