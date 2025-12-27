package org.clokey.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {
    @Autowired private FirebaseProperties firebaseProperties;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        FirebaseApp firebaseApp = getFirebaseApp();
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private FirebaseApp getFirebaseApp() throws IOException {
        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();

        if (!firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (FirebaseApp.DEFAULT_APP_NAME.equals(app.getName())) {
                    return app;
                }
            }
        }

        String path = firebaseProperties.getCredentialsPath();
        if (path == null || path.isBlank()) {
            throw new IllegalStateException("FIREBASE_CREDENTIALS_PATH is empty");
        }

        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(path)))
                        .build();

        return FirebaseApp.initializeApp(options);
    }
}
