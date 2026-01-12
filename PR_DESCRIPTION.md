## 📌 이슈 번호
- closed #76

## 🍬 기능 설명
### 신규 가입 포인트 보상 시스템 구현

OAuth2 로그인을 통한 신규 회원 가입 시 자동으로 1000 포인트를 지급하는 기능을 구현했습니다.

#### 주요 구현 사항

1. **포인트 보상 설정 관리**
   - `application.yml`에 신규 가입 보상 포인트 설정 추가 (1000 포인트)
   - `PointProperties` 설정 클래스 생성으로 설정값 중앙 관리

2. **포인트 지급 로직**
   - `PointService.grantPoints()` 메서드 추가
     - 포인트 로그 자동 생성 (EARN 상태)
     - 1년 만료 기한 자동 설정
     - 회원 포인트 잔액 업데이트
   - `Member.addPoint()` 메서드 추가 (포인트 증가 처리)

3. **OAuth2 회원가입 플로우 통합**
   - `CustomOAuth2UserService`에서 신규 회원 생성 시 자동으로 보상 지급
   - 포인트 지급 완료 로그 기록

#### 동작 플로우
```
OAuth2 로그인 성공
    ↓
신규 회원 여부 확인
    ↓
신규 회원 → Member 생성 및 저장
    ↓
PointService.grantPoints() 호출
    ↓
PointLog 생성 (NEW_MEMBER, EARN, 1000포인트)
    ↓
Member.point 증가 (0 → 1000)
```

#### 변경 파일
- `src/main/java/site/beilsang/beilsang_server_v2/global/config/PointProperties.java` (신규)
- `src/main/java/site/beilsang/beilsang_server_v2/domain/member/entity/Member.java`
- `src/main/java/site/beilsang/beilsang_server_v2/domain/point/service/PointService.java`
- `src/main/java/site/beilsang/beilsang_server_v2/global/oauth/CustomOAuth2UserService.java`
- `src/main/resources/application.yml`

## 🤔 코드 리뷰에서 집중적으로 볼 내용

### 1. 트랜잭션 처리
`CustomOAuth2UserService`에서 회원 생성과 포인트 지급이 순차적으로 이루어지는데, 트랜잭션 처리가 적절한지 확인 필요합니다.

```java
// CustomOAuth2UserService.java:68-73
member = MemberAssembler.toEntity(provider, attributes.getOAuth2UserInfo());
memberRepository.save(member);

// 신규 가입 보상 지급
int newMemberReward = pointProperties.getNewMemberReward();
pointService.grantPoints(member, newMemberReward, PointName.NEW_MEMBER);
```

- `memberRepository.save(member)` 후 즉시 `pointService.grantPoints()`를 호출하는데, 같은 트랜잭션 내에서 처리되는지?
- 포인트 지급 실패 시 회원 생성도 롤백되어야 하는지, 아니면 회원은 생성되고 포인트만 실패 처리해야 하는지?

### 2. 설정값 관리
`application.yml`에 하드코딩된 1000 포인트가 적절한지, 프로필별로 다른 값을 설정할 필요는 없는지?

```yaml
# application.yml:58-60
point:
  reward:
    new-member: 1000  # 신규 가입 시 지급 포인트
```

### 3. 포인트 만료 정책
신규 가입 보상도 1년 후 만료되는 것이 맞는지 확인 필요합니다.

```java
// PointService.java:61
.expirationDate(LocalDateTime.now().plusYears(1))  // 포인트 만료 기한 설정
```

## ⭐ 기타 사항

### 향후 개선 사항
1. **출석 보상 시스템**: 현재 `PointName.ATTENDANCE`가 정의되어 있지만 미구현 상태
2. **챌린지 성공 보상**: `PointName.SUCCESS_CHALLENGE`, `SUCCESS_CHALLENGE_HOST`도 미구현
3. **포인트 지급 실패 처리**: 현재 예외 처리 로직 없음
4. **포인트 지급 알림**: 사용자에게 포인트 지급 알림 필요 여부 검토

### 참고 사항
- 빌드 성공 확인 완료 (`./gradlew build`)
- 기존 포인트 시스템과의 통합 확인 완료
- 포인트 차감(`subPoint`)과 증가(`addPoint`) 메서드 대칭성 확보
