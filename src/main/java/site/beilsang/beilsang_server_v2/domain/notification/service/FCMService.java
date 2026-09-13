package site.beilsang.beilsang_server_v2.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    private final FirebaseMessaging firebaseMessaging;

    public void sendToToken(String token, String title, String body, Map<String, String> data) {
        if (token == null || token.isBlank()) {
            log.debug("Skipping FCM send: empty token");
            return;
        }

        Message.Builder builder = Message.builder()
            .setToken(token)
            .setNotification(Notification.builder().setTitle(title).setBody(body).build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        Message message = builder.build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("FCM send success: {}", response);
        } catch (Exception e) {
            log.warn("FCM send failed: {}", e.getMessage());
            log.error("Error occurred while sending FCM message", e);
        }
    }
}

