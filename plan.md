# iOS 알림 구현 계획

## 목표
FCM/Firebase를 사용해 아래 4가지 알림 정책을 서버에서 안정적으로 발송한다.

- 참여 챌린지 시작 알림
- 챌린지 인증 알림
- 추천 챌린지 알림
- 신규 챌린지 알림

모든 알림은 챌린지 상세 페이지로 랜딩할 수 있어야 한다.

## 현재 구조에서 바꿀 점
- `domain/notification/service/AppNotificationServiceImpl`에 정책별 발송 로직이 섞여 있어 분리 필요
- `ChallengeStatusScheduler`는 상태 변경만 담당하고 있어 알림 스케줄과 분리 가능
- `ChallengeNotification`은 `challengeId`만 들고 있어 랜딩용 payload를 맞추기 충분함
- `Member.keyword`가 단일 관심 카테고리라 추천 알림 대상 선정 기준으로 사용 가능
- `Feed.uploadDate`와 `ChallengeMember.isFeedUpload/successDays`를 조합해 인증 여부를 판단해야 함

## 구현 계획 및 진행 상황

### 1. 알림 공통 구조 정리
- [ ] `AppNotificationService`의 역할을 조회/읽음 처리와 발송 작업으로 분리한다.
- [x] FCM 전송 DTO에 `challengeId`, `notificationType` 같은 랜딩 식별자 포함 여부를 정리한다. (FCMService 및 Scheduler에 적용 완료)
- [ ] `NotificationAssembler`가 `ChallengeNotification`의 랜딩 정보를 일관되게 반환하도록 맞춘다.

### 2. 참여 챌린지 시작 알림
- 대상: `startDate`가 내일인 챌린지의 참여자
- 시점: 매일 오후 6시
- 작업:
  - [x] `ChallengeRepository`에서 내일 시작 챌린지 조회 메서드 사용 또는 추가
  - [x] `ChallengeMemberRepository`로 해당 챌린지 참여자 일괄 조회
  - [ ] 챌린지별로 1회만 발송되도록 중복 방지 기준 추가 (현재 쿼리와 발송 로직은 있으나, DB 조회 기반 중복 방지 로직 누락)

### 3. 챌린지 인증 알림
- 대상: 오늘 기준 참여 중인 챌린지 중 아직 인증하지 않은 멤버
- 시점: 매일 오후 12시
- 작업:
  - [x] 오늘 업로드된 `Feed`가 있는지 조회 (`isFeedUpload` 활용)
  - [x] `ChallengeMember.isFeedUpload`와 `successDays` / 챌린지 목표일수를 함께 확인
  - [x] 이미 인증했거나 이미 달성한 멤버는 제외
  - [x] 같은 날짜에 1회만 발송되도록 발송 기록 기준 추가

### 4. 추천 챌린지 알림
- 대상: 예정(시작일이 미래)인 챌린지 중 조회수(view count)가 가장 높은 챌린지
- 시점: 일요일 오후 8시
- 발송 횟수: 주 1회
- 랜딩 위치: 해당 챌린지 상세 페이지로 랜딩
- 작업:
  - [ ] 예정 챌린지(시작일 > 발송일 기준) 조회 (현재 로직은 `startDate >= 오늘`로 되어 있어 수정 필요)
  - [x] 조회수 기준 내림차순 정렬 후 상위 1개 챌린지 선정
  - [ ] 동점 처리: 동점이 발생하면 (정책 결정 필요 — 예: 최신 등록 우선 또는 랜덤 선택)
  - [ ] 회원별 관심 카테고리 필터 적용 여부 결정(전사 대상 또는 회원별 필터)
  - [x] 주 1회 발송 기록으로 중복 발송 방지

### 5. 신규 챌린지 알림
- 대상: 신규 등록된 챌린지의 관심 사용자
- 시점: 등록 다음날 오전 10시
- 작업:
  - [x] 전날 등록된 챌린지 조회
  - [x] 관심 카테고리 사용자에게 발송
  - [ ] 여러 개면 랜덤 1개만 랜딩 (현재는 등록된 모든 신규 챌린지 알림이 개별 발송되도록 되어 있어 제한 로직 추가 필요)
  - [x] 챌린지당 1회 발송 보장

### 6. 저장/조회 데이터 정리
- [ ] `AppNotification` 저장 시 정책별 공통/챌린지 알림이 제대로 구분되게 정리한다.
- [ ] `NotificationResponseDto`가 iOS에서 바로 쓸 수 있게 `challengeId`를 안정적으로 제공한다.
- [x] 필요하면 FCM 알림 payload에도 동일한 값들을 같이 넣는다.

### 7. repository / scheduler 정리
- [x] `ChallengeRepository`, `ChallengeMemberRepository`, `FeedRepository`에 필요한 조회 메서드를 추가한다.
- [x] 알림 스케줄은 `AppNotificationServiceImpl` 또는 별도 scheduler 클래스로 옮겨 책임을 분리한다. (`NotificationScheduler` 신설 완료)
- [x] `@Scheduled` cron을 정책별로 명확히 나눈다.

## 우선 수정 대상 파일
- [ ] `src/main/java/site/beilsang/beilsang_server_v2/domain/notification/service/AppNotificationServiceImpl.java`
- [ ] `src/main/java/site/beilsang/beilsang_server_v2/domain/notification/service/AppNotificationService.java`
- [ ] `src/main/java/site/beilsang/beilsang_server_v2/domain/notification/dto/NotificationAssembler.java`
- [ ] `src/main/java/site/beilsang/beilsang_server_v2/domain/notification/dto/req/NotificationRequestDto.java`
- [x] `src/main/java/site/beilsang/beilsang_server_v2/domain/challenge/repository/ChallengeRepository.java`
- [x] `src/main/java/site/beilsang/beilsang_server_v2/domain/member/repository/ChallengeMemberRepository.java`
- [ ] `src/main/java/site/beilsang/beilsang_server_v2/domain/feed/repository/FeedRepository.java`
- [x] `src/main/java/site/beilsang/beilsang_server_v2/domain/notification/service/NotificationScheduler.java` 신설 완료

## 검증 포인트
- [x] 각 알림이 정책 시간에만 발송되는지
- [△] 동일 대상에 중복 발송되지 않는지 (일부 로직 보완 필요)
- [△] 랜딩용 `challengeId`가 저장/응답/FCM payload 모두에 일관되게 들어가는지 (FCM에는 적용됨, 응답 DTO 확인 필요)
- [x] 인증 알림이 이미 인증 완료/목표 달성 멤버를 제외하는지
- [ ] 추천 챌린지가 "예정(시작일이 미래)"인 챌린지들 중 조회수 기준으로 올바르게 선정되는지
- [x] 추천 챌린지가 주 1회만 발송되며 동일 챌린지에 대해 주 단위 중복 발송이 발생하지 않는지
