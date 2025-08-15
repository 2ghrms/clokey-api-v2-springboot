package org.clokey.domain.member.service;

import org.clokey.domain.member.dto.request.ProfileUpdateRequest;

public interface MemberService {

    void updateProfile(ProfileUpdateRequest request);
}
