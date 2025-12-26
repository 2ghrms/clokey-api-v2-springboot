package org.clokey.domain.history.repository;

import org.clokey.domain.history.dto.response.StyleListResponse;

public interface StyleRepositoryCustom {
    StyleListResponse findAllStyles();
}
