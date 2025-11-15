package org.clokey;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ClokeyApiApplicationTests {

    @MockitoBean private FirebaseMessaging mockFirebaseMessaging;

    @Test
    void contextLoads() {}
}
