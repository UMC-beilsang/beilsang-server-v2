package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.global.config.QueryDslConfig;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class ChallengeRepositoryImplTest {

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    @DisplayName("ChallengeStatus.END로 종료된 챌린지만 조회된다")
    void findFinishedChallenges() {
        // given
        Challenge finished = Challenge.builder()
                .title("종료된 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(10))
                .finishDate(LocalDate.now().minusDays(1))
                .status(ChallengeStatus.END)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        Challenge ongoing = Challenge.builder()
                .title("진행중 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(1))
                .finishDate(LocalDate.now().plusDays(5))
                .status(ChallengeStatus.IN_PROGRESS)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        challengeRepository.save(finished);
        challengeRepository.save(ongoing);

        ChallengeListReqDTO dto = new ChallengeListReqDTO();
        dto.setChallengeStatus(ChallengeStatus.END);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Challenge> result = challengeRepository.findChallenges(dto, pageable);

        // then
        assertThat(result.stream().count()).isEqualTo(1);
        assertThat(result.getContent()).extracting("title").containsExactly("종료된 챌린지");
    }

    @Test
    @DisplayName("ChallengeStatus.IN_PROGRESS로 진행중인 챌린지만 조회된다")
    void findOngoingChallenges() {
        // given
        Challenge finished = Challenge.builder()
                .title("종료된 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(10))
                .finishDate(LocalDate.now().minusDays(1))
                .status(ChallengeStatus.END)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        Challenge ongoing = Challenge.builder()
                .title("진행중 챌린지")
                .category(Category.BIKE)
                .startDate(LocalDate.now().minusDays(1))
                .finishDate(LocalDate.now().plusDays(5))
                .status(ChallengeStatus.IN_PROGRESS)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        challengeRepository.save(finished);
        challengeRepository.save(ongoing);

        ChallengeListReqDTO dto = new ChallengeListReqDTO();
        dto.setChallengeStatus(ChallengeStatus.IN_PROGRESS);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Challenge> result = challengeRepository.findChallenges(dto, pageable);

        // then
        assertThat(result.getContent()).extracting("title").containsExactly("진행중 챌린지");
    }

    @Test
    @DisplayName("ChallengeStatus로 직접 필터링이 가능하다")
    void findByChallengeStatus() {
        // given
        Challenge notYet = Challenge.builder()
                .title("시작 전 챌린지")
                .category(Category.PLOGGING)
                .startDate(LocalDate.now().plusDays(3))
                .finishDate(LocalDate.now().plusDays(10))
                .status(ChallengeStatus.NOT_YET)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        Challenge inProgress = Challenge.builder()
                .title("진행중 챌린지")
                .category(Category.PLOGGING)
                .startDate(LocalDate.now().minusDays(1))
                .finishDate(LocalDate.now().plusDays(5))
                .status(ChallengeStatus.IN_PROGRESS)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        Challenge ended = Challenge.builder()
                .title("종료된 챌린지")
                .category(Category.PLOGGING)
                .startDate(LocalDate.now().minusDays(10))
                .finishDate(LocalDate.now().minusDays(1))
                .status(ChallengeStatus.END)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("설명")
                .joinPoint(100)
                .build();
        challengeRepository.save(notYet);
        challengeRepository.save(inProgress);
        challengeRepository.save(ended);

        ChallengeListReqDTO dto = new ChallengeListReqDTO();
        dto.setChallengeStatus(ChallengeStatus.IN_PROGRESS);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Challenge> result = challengeRepository.findChallenges(dto, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).extracting("title").containsExactly("진행중 챌린지");
    }

    @Test
    @DisplayName("키워드로 제목과 상세내용을 검색할 수 있다")
    void findByKeyword() {
        // given
        Challenge challenge1 = Challenge.builder()
                .title("플로깅 챌린지")
                .category(Category.PLOGGING)
                .startDate(LocalDate.now())
                .finishDate(LocalDate.now().plusDays(7))
                .status(ChallengeStatus.IN_PROGRESS)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("환경보호를 위한 활동")
                .joinPoint(100)
                .build();
        Challenge challenge2 = Challenge.builder()
                .title("러닝 챌린지")
                .category(Category.PLOGGING)
                .startDate(LocalDate.now())
                .finishDate(LocalDate.now().plusDays(7))
                .status(ChallengeStatus.IN_PROGRESS)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("건강한 플로깅을 해봅시다")
                .joinPoint(100)
                .build();
        challengeRepository.save(challenge1);
        challengeRepository.save(challenge2);

        ChallengeListReqDTO dto = new ChallengeListReqDTO();
        dto.setKeyword("플로깅");
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Challenge> result = challengeRepository.findChallenges(dto, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("title").containsExactlyInAnyOrder("플로깅 챌린지", "러닝 챌린지");
    }
}