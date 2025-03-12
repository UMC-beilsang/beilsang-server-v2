package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

@Getter
public class CreateChallengeReqDTO {
    private String title; // 챌린지 제목
    private LocalDate startDate; // 챌린지 시작 날짜
    private ChallengePeriod period; // 챌린지 기간(일주일/한달)
    private Integer totalGoalDay; // 목표 실천 일수
    private Category category; // 카테고리
    private String details; // 챌린지 세부설명
    private List<String> notes; // 챌린지 유의사항
    private Integer joinPoint; // 참여 포인트
}
