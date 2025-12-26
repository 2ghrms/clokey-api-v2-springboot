package org.clokey.domain.term.service;

import org.clokey.domain.term.dto.request.TermAgreeRequest;
import org.clokey.domain.term.dto.response.MyOptionalTermResponse;
import org.clokey.domain.term.dto.response.TermListResponse;

public interface TermService {

    TermListResponse getTerms();

    void agreeTerm(TermAgreeRequest request);

    MyOptionalTermResponse getMyOptionalTerms();

    void toggleMyOptionalTerms(Long termId);
}
