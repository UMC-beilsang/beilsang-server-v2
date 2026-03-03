package site.beilsang.beilsang_server_v2.global.util;

import java.util.List;
import java.util.Random;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Slf4j
@ConfigurationProperties(prefix = "nickname")
@Setter
public class NicknameGenerator {

    private List<String> adjectives;
    private List<String> nouns;
    private final Random random = new Random();

    /**
     * 랜덤 닉네임 생성: 형용사 + 명사 + 번호(000~999)
     * 예: 푸른토끼042, 꾸준한새싹123
     */
    public String generateRandomNickname() {
        if (adjectives == null || adjectives.isEmpty() || nouns == null || nouns.isEmpty()) {
            log.error("Nickname configuration is not loaded properly");
            throw new IllegalStateException("Nickname configuration is missing");
        }

        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String noun = nouns.get(random.nextInt(nouns.size()));
        int number = random.nextInt(1000); // 0~999

        return String.format("%s%s%03d", adjective, noun, number);
    }
}
