package site.beilsang.beilsang_server_v2.domain.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() {
        try{
            if (!FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.getInstance();
            }

            // gcp 서버의 IAM 권한을 자동으로 읽어옴
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build();

//            // resources/firebase-service-account.json 파일에서 직접 읽기
//            // (파일명은 실제 저장하신 JSON 파일명으로 변경하세요)
//            InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
//
//            FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase App initialized successfully");

            return app;
        }catch (IOException e) {
            log.error("FCM Key Loading Failed", e);
            throw new RuntimeException("Failed to initialize Firebase App", e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }
}

