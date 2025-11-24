package org.clokey.domain.notification.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.clokey.domain.notification.dto.response.UnreadNotificationResponse;
import org.clokey.domain.notification.service.CodiveNotificationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CodiveNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
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
}
