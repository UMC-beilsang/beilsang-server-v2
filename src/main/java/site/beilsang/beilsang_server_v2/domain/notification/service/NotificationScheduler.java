package site.beilsang.beilsang_server_v2.domain.notification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.notification.entity.ChallengeNotification;
import site.beilsang.beilsang_server_v2.domain.notification.repository.NotificationRepository;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;
    private final FCMService fcmService;

    // 0. 자정에 일일 인증 플래그 초기화
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void resetDailyFeedUploadStatus() {
        log.info("Running daily feed upload status reset scheduler");

        int updatedCount = challengeMemberRepository
            .resetFeedUploadStatusByChallengeMemberStatus(ChallengeMemberStatus.ONGOING);

        log.info("Daily feed upload status reset completed. Updated {} members", updatedCount);
    }

    // 1. 주간 추천 챌린지 알림 (매주 일요일 오후 8시)
    @Scheduled(cron = "0 0 20 * * SUN", zone = "Asia/Seoul")
    @Transactional
    public void sendWeeklyRecommendedChallenge() {
        log.info("Running weekly recommended challenge scheduler");

        var list = challengeRepository.findRecommendedChallenges(1);
        if (list == null || list.isEmpty()) {
            log.info("No recommended challenges found");
            return;
        }

        var challenge = list.get(0);

        // 중복 발송 방지: 최근 7일 내 동일 챌린지 추천 알림이 있으면 스킵
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        boolean alreadySent = notificationRepository
            .existsByChallengeIdAndCreatedAtAfter(challenge.getId(), weekAgo);

        if (alreadySent) {
            log.info("Recommended challenge {} was already sent within a week, skipping", challenge.getId());
            return;
        }

        // 메모리 이슈 방지: 500명씩 끊어서 페이징 조회
        int page = 0;
        int size = 500;
        Page<Member> memberPage;

        do {
            memberPage = memberRepository.findAll(PageRequest.of(page, size));

            for (Member m : memberPage.getContent()) {
                sendFCMAndSaveNotification(
                    m,
                    challenge.getId(),
                    "추천 챌린지: " + challenge.getTitle(),
                    challenge.getTitle() + " 챌린지를 확인해보세요!",
                    "RECOMMEND_CHALLENGE"
                );
            }
            page++;
        } while (memberPage.hasNext());

        log.info("Weekly recommended challenge notifications created for challenge {}", challenge.getId());
    }

    // 2. 참여 챌린지 시작 알림 (매일 오후 6시)
    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendChallengeStartNotification() {
        log.info("Running daily challenge start notification scheduler");

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<Challenge> startingChallenges = challengeRepository.findAllByStartDateAndIsHiddenFalse(tomorrow);

        for (Challenge challenge : startingChallenges) {
            List<ChallengeMember> challengeMembers = challengeMemberRepository
                .findAllByChallengeIdAndChallengeMemberStatusIn(
                    challenge.getId(),
                    List.of(ChallengeMemberStatus.NOT_YET, ChallengeMemberStatus.ONGOING));

            for (ChallengeMember cm : challengeMembers) {
                Member member = cm.getMember();

                boolean alreadySent = notificationRepository
                    .existsByChallengeIdAndMemberIdAndCreatedAtAfter(challenge.getId(), member.getId(), startOfDay);

                if (!alreadySent) {
                    sendFCMAndSaveNotification(
                        member,
                        challenge.getId(),
                        "내일 챌린지가 시작돼요!",
                        challenge.getTitle() + " 챌린지 준비 되셨나요?",
                        "START_CHALLENGE"
                    );
                }
            }
        }
    }

    // 3. 챌린지 인증 유도 알림 (매일 오후 12시)
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendChallengeCertNotification() {
        log.info("Running daily challenge certification notification scheduler");

        List<ChallengeMember> uncertifiedMembers = challengeMemberRepository
            .findOngoingAndUncertifiedMembers(ChallengeMemberStatus.ONGOING);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        for (ChallengeMember cm : uncertifiedMembers) {
            Challenge challenge = cm.getChallenge();
            Member member = cm.getMember();

            boolean alreadySent = notificationRepository
                .existsByChallengeIdAndMemberIdAndCreatedAtAfter(challenge.getId(), member.getId(), startOfDay);

            if (!alreadySent) {
                sendFCMAndSaveNotification(
                    member,
                    challenge.getId(),
                    "오늘의 챌린지를 인증해주세요!",
                    challenge.getTitle() + " 인증을 완료하고 포인트를 받아가세요.",
                    "CERT_CHALLENGE"
                );
            }
        }
    }

    // 4. 신규 챌린지 알림 (등록 다음날 오전 10시)
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    @Transactional
    public void sendNewChallengeNotification() {
        log.info("Running daily new challenge notification scheduler");

        LocalDateTime startOfYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endOfYesterday = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        List<Challenge> newChallenges = challengeRepository
            .findAllByCreatedAtBetweenAndIsHiddenFalse(startOfYesterday, endOfYesterday);

        if (newChallenges.isEmpty()) return;

        Random random = new Random();
        Challenge selectedChallenge = newChallenges.get(random.nextInt(newChallenges.size()));
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        // 메모리 이슈 방지: 관심 유저를 500명씩 페이징 처리
        int size = 500;
        int page = 0;
        Page<Member> interestedMembersPage;

        do {
            interestedMembersPage = memberRepository.findAllByKeyword(
                selectedChallenge.getCategory(), PageRequest.of(page, size));

            for (Member member : interestedMembersPage.getContent()) {
                boolean alreadySent = notificationRepository
                    .existsByChallengeIdAndMemberIdAndCreatedAtAfter(
                        selectedChallenge.getId(), member.getId(), startOfToday);

                if (!alreadySent) {
                    sendFCMAndSaveNotification(
                        member,
                        selectedChallenge.getId(),
                        "새로운 챌린지가 등록되었어요!",
                        selectedChallenge.getTitle() + " 챌린지에 참여해보는 건 어떨까요?",
                        "NEW_CHALLENGE"
                    );
                }
            }
            page++;
        } while (interestedMembersPage.hasNext());
    }

    // 공통: 알림 저장 및 FCM 발송 처리 메서드
    private void sendFCMAndSaveNotification(Member member, Long challengeId, String title, String body, String type) {
        try {
            ChallengeNotification notification = ChallengeNotification.builder()
                .title(title)
                .contents(body)
                .isRead(false)
                .member(member)
                .challengeId(challengeId)
                .build();

            notificationRepository.save(notification);

            if (member.getDeviceToken() != null && !member.getDeviceToken().isBlank()) {
                fcmService.sendToToken(
                    member.getDeviceToken(),
                    title,
                    body,
                    java.util.Map.of(
                        "challengeId", String.valueOf(challengeId),
                        "notificationType", type
                    )
                );
            }
        } catch (Exception e) {
            log.warn("Notification error for member {}: {}", member.getId(), e.getMessage());
        }
    }
}
