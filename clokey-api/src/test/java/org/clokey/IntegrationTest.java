package org.clokey;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTest {

    @Autowired protected DatabaseCleaner databaseCleaner;
    @MockitoBean private FirebaseMessaging mockFirebaseMessaging;

    @BeforeEach
    void setUp() {
        databaseCleaner.execute();
    }
}
