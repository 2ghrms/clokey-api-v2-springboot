package org.clokey.domain.notification.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(CodiveNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {}
