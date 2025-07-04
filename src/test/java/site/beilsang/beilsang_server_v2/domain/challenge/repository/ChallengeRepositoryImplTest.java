package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListRequestDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.global.config.QueryDslConfig;
import site.beilsang.beilsang_server_v2.global.enums.Category;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class ChallengeRepositoryImplTest {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    @DisplayName("isFinished=true이면 종료된 챌린지만 조회된다")
    void findFinishedChallenges() {
        // given
        Challenge finished = Challenge.builder()
                .title("종료된 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(10))
                .finishDate(LocalDate.now().minusDays(1))
                .build();
        Challenge ongoing = Challenge.builder()
                .title("진행중 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(1))
                .finishDate(LocalDate.now().plusDays(5))
                .build();
        challengeRepository.save(finished);
        challengeRepository.save(ongoing);

        ChallengeListRequestDTO dto = new ChallengeListRequestDTO();
        dto.setIsFinished(true);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Challenge> result = challengeRepository.findChallenges(dto, pageable);

        // then
        assertThat(result.stream().count()).isEqualTo(1);
        assertThat(result.getContent()).extracting("title").containsExactly("종료된 챌린지");
    }

    @Test
    @DisplayName("isFinished=false이면 아직 종료되지 않은 챌린지만 조회된다")
    void findOngoingChallenges() {
        // given
        Challenge finished = Challenge.builder()
                .title("종료된 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(10))
                .finishDate(LocalDate.now().minusDays(1))
                .build();
        Challenge ongoing = Challenge.builder()
                .title("진행중 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(1))
                .finishDate(LocalDate.now().plusDays(5))
                .build();
        challengeRepository.save(finished);
        challengeRepository.save(ongoing);

        ChallengeListRequestDTO dto = new ChallengeListRequestDTO();
        dto.setIsFinished(false);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Challenge> result = challengeRepository.findChallenges(dto, pageable);

        // then
        assertThat(result.getContent()).extracting("title").containsExactly("진행중 챌린지");
    }
}