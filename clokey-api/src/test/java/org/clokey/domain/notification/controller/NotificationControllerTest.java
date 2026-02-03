package org.clokey.domain.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.clokey.domain.notification.dto.request.TemperatureNotificationRequest;
import org.clokey.domain.notification.dto.response.NotificationListResponse;
import org.clokey.domain.notification.dto.response.UnreadNotificationResponse;
import org.clokey.domain.notification.service.CodiveNotificationService;
import org.clokey.notification.enums.NotificationType;
import org.clokey.notification.enums.ReadStatus;
import org.clokey.notification.enums.RedirectType;
import org.clokey.response.SliceResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest(CodiveNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CodiveNotificationService codiveNotificationService;

    @Nested
    class 안읽은_알림_여부_요청_시 {

        @Test
        void 유효한_요청이면_안읽은_알림_여부를_반환한다() throws Exception {
            // given
            UnreadNotificationResponse response = new UnreadNotificationResponse(false);
            given(codiveNotificationService.existsUnreadNotification()).willReturn(response);

            // when
            ResultActions perform = mockMvc.perform(get("/notifications/not-read-exist"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.existsUnreadNotification").value(false));
        }
    }

    @Nested
    class 알림_목록_조회할_시 {

        @Test
        void 유효한_요청이면_알림_목록을_반환한다() throws Exception {
            // given
            List<NotificationListResponse> notifications =
                    List.of(
                            new NotificationListResponse(
                                    2L,
                                    "https://image.example",
                                    "테스트 알림2 내용입니다.",
                                    NotificationType.COMMENT,
                                    new NotificationListResponse.NotificationActionResponse(
                                            RedirectType.HISTORY_REDIRECT, "2"),
                                    ReadStatus.NOT_READ,
                                    LocalDateTime.now()),
                            new NotificationListResponse(
                                    1L,
                                    "https://image.example",
                                    "테스트 알림1 내용입니다.",
                                    NotificationType.FOLLOW,
                                    new NotificationListResponse.NotificationActionResponse(
                                            RedirectType.MEMBER_REDIRECT, "2"),
                                    ReadStatus.READ,
                                    LocalDateTime.now()));

            given(codiveNotificationService.getNotificationList(null, 10))
                    .willReturn(new SliceResponse<NotificationListResponse>(notifications, true));

            // when
            ResultActions perform = mockMvc.perform(get("/notifications").param("size", "10"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.content[0].notificationId").value("2"))
                    .andExpect(jsonPath("$.result.content[0].notificationType").value("COMMENT"))
                    .andExpect(
                            jsonPath("$.result.content[0].action.redirectType")
                                    .value("HISTORY_REDIRECT"))
                    .andExpect(jsonPath("$.result.content[0].action.redirectInfo").value("2"))
                    .andExpect(jsonPath("$.result.content[1].notificationId").value("1"))
                    .andExpect(jsonPath("$.result.content[1].notificationType").value("FOLLOW"))
                    .andExpect(
                            jsonPath("$.result.content[1].action.redirectType")
                                    .value("MEMBER_REDIRECT"))
                    .andExpect(jsonPath("$.result.content[1].action.redirectInfo").value("2"))
                    .andExpect(jsonPath("$.result.isLast").value(true));
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "-999", "0"})
        void 페이지_크기를_0_이하로_설정하면_예외가_발생한다(String pageSize) throws Exception {
            // when & then
            ResultActions perform = mockMvc.perform(get("/notifications").param("size", pageSize));

            perform.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value("COMMON400"))
                    .andExpect(jsonPath("$.message").value("페이지 크기는 0보다 큰 값만 가능합니다."));
        }
    }

    @Nested
    class 알림_읽음_처리_요청_시 {

        @Test
        void 유효한_요청이면_알림_상태를_읽음으로_변경한다() throws Exception {
            // given
            willDoNothing().given(codiveNotificationService).updateReadStatus(1L);

            // when
            ResultActions perform = mockMvc.perform(patch("/notifications/1"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 알림_전체_읽음_처리_요청_시 {

        @Test
        void 유효한_요청이면_전체_알림_상태를_읽음으로_변경한다() throws Exception {
            // given
            willDoNothing().given(codiveNotificationService).updateAllReadStatus();

            // when
            ResultActions perform = mockMvc.perform(patch("/notifications/read-all"));

            // then
            perform.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON204"))
                    .andExpect(jsonPath("$.message").value("요청 성공 및 반환값 없음"));
        }
    }

    @Nested
    class 오늘의_온도_알림_발송_요청_시 {

        @Test
        void 유효한_요청이면_오늘의_온도_알림을_발송한다() throws Exception {
            // given
            TemperatureNotificationRequest request = new TemperatureNotificationRequest(-5.5);
            willDoNothing()
                    .given(codiveNotificationService)
                    .sendNewTemperatureNotification(request);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/notifications/today-temperature")
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
        void 오늘의_온도를_비워두면_예외가_발생한다(Double temp) throws Exception {
            // given
            TemperatureNotificationRequest request = new TemperatureNotificationRequest(temp);

            // when
            ResultActions perform =
                    mockMvc.perform(
                            post("/notifications/today-temperature")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)));

            // then
            perform.andExpect(status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(false))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("COMMON400"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("잘못된 요청입니다."))
                    .andExpect(
                            MockMvcResultMatchers.jsonPath("$.result.temperature")
                                    .value("온도는 비워둘 수 없습니다."));
        }
    }
}
