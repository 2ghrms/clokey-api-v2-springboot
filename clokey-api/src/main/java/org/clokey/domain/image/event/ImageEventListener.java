package org.clokey.domain.image.event;

import lombok.RequiredArgsConstructor;
import org.clokey.util.StorageUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ImageEventListener {

    private final StorageUtil storageUtil;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleImagesDeleteEvent(ImagesDeleteEvent event) {
        storageUtil.deleteAllByUrls(event.imageUrls());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleImageDeleteEvent(ImageDeleteEvent event) {
        storageUtil.deleteByUrl(event.imageUrl());
    }
}
