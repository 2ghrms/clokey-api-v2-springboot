package org.clokey.domain.cloth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.clokey.IntegrationTest;
import org.clokey.category.entity.Category;
import org.clokey.domain.category.exception.CategoryErrorCode;
import org.clokey.domain.category.repository.CategoryRepository;
import org.clokey.domain.cloth.dto.request.ClothCreateRequest;
import org.clokey.domain.cloth.dto.request.ClothCreateRequests;
import org.clokey.domain.cloth.repository.ClothRepository;
import org.clokey.domain.member.repository.MemberRepository;
import org.clokey.exception.BaseCustomException;
import org.clokey.global.FakeAuthContext;
import org.clokey.member.entity.Member;
import org.clokey.member.entity.OauthInfo;
import org.clokey.member.enums.MemberStatus;
import org.clokey.member.enums.OauthProvider;
import org.clokey.member.enums.RegisterStatus;
import org.clokey.member.enums.Visibility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class ClothServiceTest extends IntegrationTest {

    @Autowired private ClothService clothService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ClothRepository clothRepository;
    @Autowired private CategoryRepository categoryRepository;

    @MockitoBean FakeAuthContext fakeAuthContext;

    @Nested
    class 옷을_생성할_때 {

        @BeforeEach
        void setUp() {
            Member member =
                    Member.createMember(
                            "testEmail",
                            "testClokeyId",
                            "testNickName",
                            OauthInfo.createOauthInfo("testOauthId", OauthProvider.KAKAO),
                            MemberStatus.ACTIVE,
                            RegisterStatus.REGISTERED,
                            Visibility.PUBLIC);

            memberRepository.save(member);
            given(fakeAuthContext.getCurrentMember()).willReturn(member);

            Category category = Category.createCategory("testCategory", null);
            categoryRepository.save(category);
        }

        @Test
        void 유효한_요청이면_옷을_생성한다() {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest("testClothImageUrl1", 1L),
                                    new ClothCreateRequest("testClothImageUrl2", 1L)));

            // when
            clothService.createCloths(request);

            // then
            Assertions.assertAll(
                    () ->
                            assertThat(clothRepository.findById(1L).orElseThrow())
                                    .extracting("clothImageUrl", "category.id", "member.id")
                                    .containsExactly("testClothImageUrl1", 1L, 1L),
                    () ->
                            assertThat(clothRepository.findById(2L).orElseThrow())
                                    .extracting("clothImageUrl", "category.id", "member.id")
                                    .containsExactly("testClothImageUrl2", 1L, 1L));
        }

        @Test
        void 카테고리가_존재하지_않을_경우_예외가_발생한다() {
            // given
            ClothCreateRequests request =
                    new ClothCreateRequests(
                            List.of(
                                    new ClothCreateRequest("testClothImageUrl1", 1L),
                                    new ClothCreateRequest("testClothImageUrl2", 999L)));

            // when & then
            assertThatThrownBy(() -> clothService.createCloths(request))
                    .isInstanceOf(BaseCustomException.class)
                    .hasMessage(CategoryErrorCode.CATEGORY_IN_BULK_NOT_FOUND.getMessage());
        }
    }
}
