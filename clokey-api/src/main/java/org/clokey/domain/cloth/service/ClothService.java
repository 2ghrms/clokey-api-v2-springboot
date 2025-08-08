package org.clokey.domain.cloth.service;

import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.dto.response.ClothCreateResponse;

public interface ClothService {

    ClothCreateResponse createCloths(ClothCreateRequests list);
}
