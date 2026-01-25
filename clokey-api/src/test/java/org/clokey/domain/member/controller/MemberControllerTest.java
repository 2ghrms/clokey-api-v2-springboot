package org.clokey.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.clokey.domain.member.dto.request.DuplicatedIdCheckRequest;
import org.clokey.domain.member.dto.request.ProfileUpdateRequest;
import org.clokey.domain.member.dto.response.*;
import org.clokey.domain.member.service.MemberService;
import org.clokey.global.paging.SortDirection;
import org.clokey.member.enums.Visibility;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MemberService memberService;

    @Nested
    class 프로필_수정_요청_시 {

        @Test
        void 유효한_요청이면_성공코드를_반환한다() throws Exception {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "testNickname",
                            "testBio",
                            Visibility.PUBLIC,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            willDoNothing().given(memberService).updateProfile(any(ProfileUpdateRequest.class));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        @ValueSource(strings = {" "})
        void 닉네임_비어있으면_예외가_발생한다(String nickname) throws Exception {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            nickname,
                            "testBio",
                            Visibility.PRIVATE,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.nickname").value("닉네임은 비워둘 수 없습니다."));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void 닉네임이_비어있으면_예외가_발생한다(String nickname) throws Exception {
            // given
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            nickname,
                            "testBio",
                            Visibility.PRIVATE,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.nickname").value("닉네임은 비워둘 수 없습니다."));
        }

        @Test
        void 바이오가_100자를_초과하면_예외가_발생한다() throws Exception {
            // given
            String longBio = "a".repeat(101);
            ProfileUpdateRequest request =
                    new ProfileUpdateRequest(
                            "testNickname",
                            longBio,
                            Visibility.PRIVATE,
                            "https://img.example.com/bg.jpg",
                            "https://img.example.com/bg.jpg");

            // when
            ResultActions perform =
                    mockMvc.perform(
                            patch("/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.bio").value("바이오는 100자를 넘길 수 없습니다."));
        }
    }

    @Nested
    class 아이디_중복확인_요청_시 {

        @Test
        void 유효한_요청이면_중복_여부를_반환한다() throws Exception {
            // given
            DuplicatedIdCheckRequest request = new DuplicatedIdCheckRequest("test_clokey_id");
            DuplicatedIdCheckResponse response = new DuplicatedIdCheckResponse(true);

            given(memberService.checkDuplicateNickname(request)).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/users/check-duplicate-nickname")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.duplicated").value(true));
        }

        @Test
        void 닉네임이_null이면_예외가_발생한다() throws Exception {
            // given
            DuplicatedIdCheckRequest request = new DuplicatedIdCheckRequest(null);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/users/check-duplicate-nickname")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(jsonPath("$.result.nickname").value("닉네임은 비워둘 수 없습니다."));
        }

        // 허용 종류 : 영문(소문자) , 숫자, 언더바(_), 점(.)
        @ParameterizedTest
        @ValueSource(strings = {"clokey clokey", "CLOKEY", "클로키", "clokey-user", "clokey,,user^^"})
        void 닉네임_제약조건을_위배하면_예외가_발생한다(String nickname) throws Exception {
            // given
            DuplicatedIdCheckRequest request = new DuplicatedIdCheckRequest(nickname);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/users/check-duplicate-nickname")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            jsonPath("$.result.nickname")
                                    .value("닉네임은 영어 소문자, 숫자, 언더바(_), 점(.)만 허용됩니다."));
        }
    }

    @Nested
    class 공개계정_팔로우_요청_시 {

        @Test
        void 유효한_요청이면_성공코드를_반환한다() throws Exception {
            // given
            long targetId = 1L;

            willDoNothing().given(memberService).toggleFollow(targetId);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/users/follow")
                                    .param("userId", String.valueOf(targetId))
                                    .contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 비공개계정_팔로우_요청_시 {

        @Test
        void 유효한_요청이면_성공코드를_반환한다() throws Exception {
            // given
            long targetId = 1L;

            willDoNothing().given(memberService).togglePendingFollow(targetId);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/users/pending-follow")
                                    .param("userId", String.valueOf(targetId))
                                    .contentType(MediaType.APPLICATION_JSON));
            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 차단_토글_요청_시 {

        @Test
        void 유효한_요청이면_차단을_토글하고_NO_CONTENT를_반환한다() throws Exception {
            // given
            willDoNothing().given(memberService).toggleBlockStatus(1L);

            // when
            ResultActions perform =
                    mockMvc.perform(post("/users/block/1").contentType(MediaType.APPLICATION_JSON));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 본인_확인_요청_시 {

        @Test
        void 유효한_요청이면_본인인지_여부를_반환한다() throws Exception {
            // given
            String nickname = "test123";
            MyselfCheckResponse response = new MyselfCheckResponse(true);

            given(memberService.checkIsMyself(nickname)).willReturn(response);

            // when
            ResultActions perform =
                    mockMvc.perform(get("/users/check-myself").param("nickname", nickname));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.isMyself").value(true));
        }
    }

    @Nested
    class 차단_회원_조회_요청_시 {

        @Test
        void 정렬_조건이_DESC이면_blockId를_내림차순으로_응답한다() throws Exception {
            // given
            List<BlockedMemberResponse> blockedMembers =
                    List.of(
                            new BlockedMemberResponse(2L, 2L, "testId2", "testUrl2"),
                            new BlockedMemberResponse(1L, 1L, "testId1", "testUrl1"));

            given(memberService.getBlockedMembers(null, 2, SortDirection.DESC))
                    .willReturn(new SliceResponse<BlockedMemberResponse>(blockedMembers, true));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/blocks").param("size", "2").param("direction", "DESC"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].blockId").value(2L))
                    .andExpect(jsonPath("$.result.content[1].blockId").value(1L))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 정렬_조건이_ASC이면_createdAt을_오름차순으로_응답한다() throws Exception {
            // given
            List<BlockedMemberResponse> blockedMembers =
                    List.of(
                            new BlockedMemberResponse(1L, 1L, "testId1", "testUrl1"),
                            new BlockedMemberResponse(2L, 2L, "testId2", "testUrl2"));

            given(memberService.getBlockedMembers(null, 2, SortDirection.ASC))
                    .willReturn(new SliceResponse<BlockedMemberResponse>(blockedMembers, true));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/blocks").param("size", "2").param("direction", "ASC"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].blockId").value(1L))
                    .andExpect(jsonPath("$.result.content[1].blockId").value(2L))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<BlockedMemberResponse> blockedMembers =
                    List.of(new BlockedMemberResponse(1L, 1L, "testId1", "testUrl1"));

            given(memberService.getBlockedMembers(null, 1, SortDirection.DESC))
                    .willReturn(new SliceResponse<BlockedMemberResponse>(blockedMembers, false));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/blocks").param("size", "1").param("direction", "DESC"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].blockId").value(1L))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/blocks").param("size", pageSize).param("direction", "ASC"));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }
    }

    @Nested
    class 팔로잉_팔로워_목록_조회_요청_시 {

        @Test
        void 유효한_요청이면_팔로잉_목록을_반환한다() throws Exception {
            // given
            List<FollowMemberResponse> followMembers =
                    List.of(
                            new FollowMemberResponse(
                                    2L,
                                    2L,
                                    "nickname1",
                                    "https://img.example.com/bg.jpg",
                                    false,
                                    false),
                            new FollowMemberResponse(
                                    1L,
                                    1L,
                                    "nickname2",
                                    "https://img.example2.com/bg.jpg",
                                    true,
                                    false));

            given(memberService.getFollows(3L, null, true, 5))
                    .willReturn(new SliceResponse<FollowMemberResponse>(followMembers, true));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/follows")
                                    .param("memberId", "3")
                                    .param("isFollowing", "true")
                                    .param("size", "5"));
            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].followId").value(2L))
                    .andExpect(jsonPath("$.result.content[1].followId").value(1L))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 유효한_요청이면_팔로워_목록을_반환한다() throws Exception {
            // given
            List<FollowMemberResponse> followMembers =
                    List.of(
                            new FollowMemberResponse(
                                    2L,
                                    2L,
                                    "nickname2",
                                    "https://img.example.com/bg.jpg",
                                    false,
                                    false),
                            new FollowMemberResponse(
                                    1L,
                                    1L,
                                    "nickname1",
                                    "https://img.example2.com/bg.jpg",
                                    true,
                                    true));

            given(memberService.getFollows(1L, null, false, 5))
                    .willReturn(new SliceResponse<FollowMemberResponse>(followMembers, true));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/follows")
                                    .param("memberId", "1")
                                    .param("isFollowing", "false")
                                    .param("size", "5"));
            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].followId").value(2L))
                    .andExpect(jsonPath("$.result.content[1].followId").value(1L))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @Test
        void 마지막_페이지가_아닌_경우_isLast를_false로_응답한다() throws Exception {
            // given
            List<FollowMemberResponse> followMembers =
                    List.of(
                            new FollowMemberResponse(
                                    2L,
                                    2L,
                                    "nickname1",
                                    "https://img.example.com/bg.jpg",
                                    true,
                                    false));

            given(memberService.getFollows(1L, null, true, 1))
                    .willReturn(new SliceResponse<FollowMemberResponse>(followMembers, false));

            // when
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/follows")
                                    .param("memberId", "1")
                                    .param("isFollowing", "true")
                                    .param("size", "1"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].followId").value(2L))
                    .andExpect(jsonPath("$.result.isLast").value(false));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform =
                    mockMvc.perform(
                            get("/users/follows")
                                    .param("memberId", "1")
                                    .param("isFollowing", "true")
                                    .param("size", pageSize));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }
    }

    @Nested
    class 회원_정보_조회_시 {

        @Test
        void 유효한_요청이면_회원_정보를_반환한다() throws Exception {
            // given
            MemberInfoResponse result =
                    new MemberInfoResponse(
                            1L,
                            "nickname123",
                            "한줄소개입니다~",
                            123L,
                            321L,
                            "https://img.example.com/bg.jpg",
                            true,
                            true,
                            false);
            given(memberService.getMemberInfo(1L)).willReturn(result);

            // when
            ResultActions perform = mockMvc.perform(get("/users/1"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.result.memberId").value(1L))
                    .andExpect(jsonPath("$.result.isPublic").value(true));
        }
    }

    @Nested
    class 내_정보_조회_요청_시 {

        @Test
        void 유효한_요청이면_내_정보를_반환한다() throws Exception {
            // given
            MyInfoResponse result =
                    new MyInfoResponse(
                            1L,
                            "myNickname",
                            "내 한줄소개",
                            "me@example.com",
                            10L,
                            5L,
                            "https://img.example.com/me.jpg",
                            true,
                            true);
            given(memberService.getMyInfo()).willReturn(result);

            // when
            ResultActions perform = mockMvc.perform(get("/users/me"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.memberId").value(1L))
                    .andExpect(jsonPath("$.result.nickname").value("myNickname"))
                    .andExpect(jsonPath("$.result.email").value("me@example.com"))
                    .andExpect(jsonPath("$.result.isPublic").value(true))
                    .andExpect(jsonPath("$.result.isMe").value(true));
        }
    }
}
