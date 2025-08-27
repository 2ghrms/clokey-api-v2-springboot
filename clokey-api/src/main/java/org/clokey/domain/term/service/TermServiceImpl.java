package org.clokey.domain.term.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.clokey.domain.term.dto.request.TermAgreeRequest;
import org.clokey.domain.term.dto.response.TermListResponse;
import org.clokey.domain.term.enums.TermInfo;
import org.clokey.domain.term.exception.TermErrorCode;
import org.clokey.domain.term.repository.MemberTermRepository;
import org.clokey.domain.term.repository.TermRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.util.MemberUtil;
import org.clokey.member.entity.Member;
import org.clokey.term.entity.MemberTerm;
import org.clokey.term.entity.Term;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermServiceImpl implements TermService {

    private final MemberUtil memberUtil;

    private final TermRepository termRepository;
    private final MemberTermRepository memberTermRepository;

    @Override
    public TermListResponse getTerms() {
        final List<Term> terms = termRepository.findAll();

        return TermListResponse.of(terms);
    }

    @Override
    @Transactional
    public void agreeTerm(TermAgreeRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        validateAllTermContained(request);
        validateAgreedNonOptionalTerms(request);

        List<MemberTerm> memberTerms =
                request.payloads().stream()
                        .map(
                                payload -> {
                                    Term term =
                                            termRepository
                                                    .findById(payload.termId())
                                                    .orElseThrow(
                                                            () ->
                                                                    new BaseCustomException(
                                                                            TermErrorCode
                                                                                    .TERM_NOT_FOUND));

                                    return MemberTerm.createMemberTerm(
                                            currentMember, term, payload.agreed());
                                })
                        .toList();

        memberTermRepository.saveAll(memberTerms);
    }

    private void validateAllTermContained(TermAgreeRequest request) {
        List<Long> allTermIds = TermInfo.getAllIds();

        List<Long> requestTermIds =
                request.payloads().stream().map(TermAgreeRequest.Payload::termId).toList();

        Set<Long> allSet = new HashSet<>(allTermIds);
        Set<Long> requestSet = new HashSet<>(requestTermIds);

        if (!allSet.equals(requestSet)) {
            throw new BaseCustomException(TermErrorCode.TERMS_MISMATCH);
        }
    }

    private void validateAgreedNonOptionalTerms(TermAgreeRequest request) {
        List<Long> requiredIds = TermInfo.getNonOptionalIds();

        List<Long> agreedIds =
                request.payloads().stream()
                        .filter(TermAgreeRequest.Payload::agreed)
                        .map(TermAgreeRequest.Payload::termId)
                        .toList();

        if (!agreedIds.containsAll(requiredIds)) {
            throw new BaseCustomException(TermErrorCode.NON_OPTIONAL_NOT_AGREED);
        }
    }
}
