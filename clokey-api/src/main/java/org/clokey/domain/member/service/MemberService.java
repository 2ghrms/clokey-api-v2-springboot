package org.clokey.domain.member.service;

import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.DuplicatedIdCheckResponse;

public interface MemberService {

    void updateProfile(ProfileUpdateRequest request);

    DuplicatedIdCheckResponse checkDuplicateClokeyId(DuplicatedIdCheckRequest request);
}
