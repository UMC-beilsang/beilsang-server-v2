package site.beilsang.beilsang_server_v2.domain.badge.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.badge.dto.BadgeAssembler;
import site.beilsang.beilsang_server_v2.domain.badge.dto.res.BadgeResDTO;
import site.beilsang.beilsang_server_v2.domain.badge.dto.res.RepresentativeBadgeResDTO;
import site.beilsang.beilsang_server_v2.domain.badge.entity.Badge;
import site.beilsang.beilsang_server_v2.domain.badge.repository.BadgeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.BadgeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.BadgeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeBadgeType;

import java.time.LocalDateTime;
import java.util.List;

import static site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final BadgeMemberRepository badgeMemberRepository;
    private final MemberRepository memberRepository;

    // ── 내 배지 전체 목록 조회 ────────────────────────────────────────────────
    @Override
    public List<BadgeResDTO> getMyBadgeList(Long memberId) {
        List<BadgeMember> memberBadges =
            badgeMemberRepository.findAllByMemberIdWithBadge(memberId);
        return BadgeAssembler.toBadgeResDTOList(memberBadges);
    }

    // ── 대표 배지 조회 ────────────────────────────────────────────────────────
    @Override
    public BadgeResDTO getRepresentativeBadge(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));

        // 대표 배지 미설정 시 null 반환
        if (member.getRepresentativeBadge() == null) {
            return null;
        }

        return BadgeAssembler.toBadgeResDTO(member.getRepresentativeBadge());
    }

    // ── 대표 배지 설정 / 해제 ─────────────────────────────────────────────────
    @Override
    @Transactional
    public void updateRepresentativeBadge(Long memberId, RepresentativeBadgeResDTO requestDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));

        // memberBadgeId == null 이면 대표 배지 해제
        if (requestDTO.getBadgeMemberId() == null) {
            member.updateRepresentativeBadge(null);
            return;
        }

        // 본인이 보유한 배지인지 확인
        BadgeMember badgeMember = badgeMemberRepository
            .findByIdAndMemberId(requestDTO.getBadgeMemberId(), memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER_BADGE));

        // 4단계(숲) 카테고리 배지인지 검증 (Member.updateRepresentativeBadge 내부에서도 검증)
        if (!badgeMember.isEligibleForRepresentative()) {
            throw new BaseException(NOT_ELIGIBLE_BADGE);
        }

        member.updateRepresentativeBadge(badgeMember);
    }
    /**
     * ChallengeService, FeedService에서 호출.
     * 이미 해당 BadgeType의 MemberBadge가 존재하면 조용히 종료 (예외 없음).
     * 호출부의 트랜잭션에 참여하므로 별도 @Transactional 불필요.
     */
    @Override
    @Transactional
    public void grantActivityBadgeIfFirst(Long memberId, ChallengeBadgeType badgeType) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));
        // 이미 보유한 배지면 중단
        if (badgeMemberRepository.existsByMemberAndChallengeBadgeType(member, badgeType)) {
            return;
        }


        // 기준 Badge 데이터 조회 (DB에 미리 insert된 기준 데이터)
        Badge badge = badgeRepository.findByBadgeType(badgeType)
            .orElseThrow(() -> new BaseException(NOT_FOUND_BADGE));

        BadgeMember memberBadge = BadgeMember.builder()
            .member(member)
            .badge(badge)
            .acquiredAt(LocalDateTime.now())
            .currentStep(null)  // 활동 배지는 단계 없음
            .build();

        badgeMemberRepository.save(memberBadge);
    }
}
