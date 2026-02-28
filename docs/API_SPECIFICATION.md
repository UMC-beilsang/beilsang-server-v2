# 비일상(Beilsang) 서버 API 명세서

## 📋 목차
1. [인증/OAuth API](#1-인증oauth-api)
2. [회원 관리 API](#2-회원-관리-api)
3. [챌린지 API](#3-챌린지-api)
4. [피드 API](#4-피드-api)
5. [발견(Achievement) API](#5-발견achievement-api)
6. [공통 응답 형식](#공통-응답-형식)
7. [에러 코드](#에러-코드)

---

## 🔐 인증 방식
- **JWT Bearer Token** 사용
- Authorization 헤더에 `Bearer {accessToken}` 형식으로 전송
- Access Token 만료 시간: **30분**
- Refresh Token 만료 시간: **14일**

### 화이트리스트 (인증 불필요)
```
/api/oauth/login/kakao
/api/oauth/login/apple
/api/oauth/refresh
/api/oauth/nickname
```

---

## 1. 인증/OAuth API

### 1.1 카카오 로그인
```http
POST /api/oauth/login/kakao
```

**Request Body**
```json
{
  "idToken": "string"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isExistMember": false
  }
}
```

**설명**
- iOS SDK에서 받은 카카오 idToken으로 로그인
- 신규 회원은 자동 가입 처리
- `isExistMember`: `false`는 신규 회원, `true`는 기존 회원

---

### 1.2 애플 로그인
```http
POST /api/oauth/login/apple
```

**Request Body**
```json
{
  "identityToken": "string",
  "authorizationCode": "string (optional)"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isExistMember": true
  }
}
```

**설명**
- iOS Sign in with Apple에서 받은 identityToken으로 로그인
- authorizationCode는 탈퇴 기능을 위해 선택적으로 전달

---

### 1.3 토큰 재발급
```http
POST /api/oauth/refresh
```

**Request Body**
```json
{
  "refreshToken": "string"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "isExistMember": true
  }
}
```

**설명**
- Access Token 만료 시 새로운 토큰 발급
- Refresh Token도 함께 재발급됨

---

### 1.4 카카오 로그아웃
```http
POST /api/oauth/logout/kakao
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

**설명**
- 서버의 Refresh Token 삭제
- 카카오 서버에도 로그아웃 요청

---

### 1.5 카카오 회원 탈퇴
```http
POST /api/oauth/unlink/kakao
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

**설명**
- 카카오 계정 연동 해제
- 회원 정보 완전 삭제 (복구 불가)

---

### 1.6 애플 회원 탈퇴
```http
POST /api/oauth/unlink/apple
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

**설명**
- Apple 계정 연동 해제
- 회원 정보 완전 삭제 (복구 불가)

---

### 1.7 닉네임 유효성 검사
```http
GET /api/oauth/nickname?nickname={nickname}
```

**Query Parameters**
- `nickname`: 검증할 닉네임 (2-10자, 한글/영문/숫자)

**Response (성공)**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

**Response (실패)**
```json
{
  "isSuccess": false,
  "code": "DUPLICATE_NICKNAME",
  "message": "이미 사용 중인 닉네임입니다."
}
```

**설명**
- 회원가입 시 닉네임 중복 및 형식 검증
- 2-10자, 한글/영문/숫자만 허용

---

## 2. 회원 관리 API

### 2.1 마이페이지 조회
```http
GET /api/mypage
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "nickName": "홍길동",
    "profileUrl": "https://example.com/profile.jpg",
    "point": 1500,
    "feedDTOs": [],
    "countFeed": 10,
    "successChallenge": 5,
    "challenges": 8,
    "failedChallenges": 3,
    "likes": 25
  }
}
```

---

### 2.2 포인트 내역 조회
```http
GET /api/mypage/point
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "currentPoint": 1500,
    "pointLogs": [
      {
        "logId": 1,
        "point": 100,
        "description": "챌린지 완료",
        "createdAt": "2024-03-15T10:30:00"
      }
    ]
  }
}
```

---

### 2.3 프로필 정보 수정
```http
PATCH /api/profile
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "nickName": "새닉네임",
  "birth": "1990-01-01",
  "gender": "MALE",
  "address": "서울특별시 강남구",
  "resolution": "환경을 보호하자"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "nickName": "새닉네임",
    "birth": "1990-01-01",
    "gender": "MALE",
    "address": "서울특별시 강남구",
    "resolution": "환경을 보호하자"
  }
}
```

**필드 설명**
- `nickName`: 닉네임 (선택)
- `birth`: 생년월일 (yyyy-MM-dd, 선택)
- `gender`: 성별 (MALE/FEMALE, 선택)
- `address`: 주소 (선택)
- `resolution`: 다짐 (선택)

---

### 2.4 프로필 이미지 수정
```http
PATCH /api/profile/image
Authorization: Bearer {accessToken}
```

**Request Body**
```json
{
  "profileUrl": "https://example.com/new-profile.jpg"
}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

---

### 2.5 챌린지 참여 여부 확인
```http
GET /api/check/{challengeId}
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `challengeId`: 확인할 챌린지 ID

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "isEnrolled": true,
    "enrolledChallengeIds": [1, 5, 10]
  }
}
```

---

## 3. 챌린지 API

### 3.1 챌린지 생성
```http
POST /challenge
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

**Request Body (multipart/form-data)**
```
data: {
  "title": "텀블러 사용 챌린지",
  "content": "일주일간 텀블러 사용하기",
  "category": "TUMBLER",
  "startDate": "2024-04-01",
  "endDate": "2024-04-07",
  "goalCount": 7,
  "maxParticipants": 100
}
infoImages: [File, File, ...]
certImages: [File, File, ...]
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "challengeId": 123
  }
}
```

**카테고리 목록**
- `TUMBLER`: 텀블러 사용
- `REFILL_STATION`: 리필스테이션
- `MULTIPLE_CONTAINERS`: 다회용기
- `ECO_PRODUCT`: 친환경제품
- `PLOGGING`: 플로깅
- `VEGAN`: 비건
- `PUBLIC_TRANSPORT`: 대중교통
- `BIKE`: 자전거
- `RECYCLE`: 재활용

---

### 3.2 모집중 챌린지 목록 조회
```http
GET /challenge/list/open?category=ALL&sortType=DEADLINE_SOON&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `category`: 카테고리 (ALL, TUMBLER, PLOGGING 등) - 기본값: ALL
- `sortType`: 정렬 기준 (DEADLINE_SOON: 마감임박순, NEWEST: 최신순) - 기본값: DEADLINE_SOON
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [
      {
        "challengeId": 1,
        "title": "텀블러 사용 챌린지",
        "category": "TUMBLER",
        "startDate": "2024-04-01",
        "endDate": "2024-04-07",
        "status": "NOT_YET",
        "attendeeCount": 45,
        "countLikes": 120,
        "isLiked": false,
        "isJoined": false
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 50,
    "totalPages": 5
  }
}
```

**설명**
- 시작일이 오늘 이후인 챌린지 조회
- 마감임박순(시작일 오름차순) 또는 최신순(생성일 내림차순) 정렬

---

### 3.3 모집마감 챌린지 목록 조회
```http
GET /challenge/list/closed?category=ALL&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `category`: 카테고리 (ALL, TUMBLER, PLOGGING 등) - 기본값: ALL
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**설명**
- 시작일이 오늘 이전인 챌린지 조회
- 최근 마감순(startDate 내림차순) 정렬

---

### 3.4 찜한 챌린지 목록 조회
```http
GET /challenge/liked?category=ALL&sortType=DEADLINE_SOON&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `category`: 카테고리 (ALL, TUMBLER, PLOGGING 등) - 기본값: ALL
- `sortType`: 정렬 기준 (DEADLINE_SOON: 마감임박순, NEWEST: 최신순) - 기본값: DEADLINE_SOON
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**설명**
- 내가 찜한 챌린지 목록 조회
- 마감임박순 또는 최신순 정렬

---

### 3.5 나의 챌린지 목록 조회
```http
GET /challenge/my?category=ALL&participationStatus=ONGOING&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `category`: 카테고리 (ALL, TUMBLER, PLOGGING 등) - 기본값: ALL
- `participationStatus`: 참여 상태
  - `ONGOING`: 참여 중 (ChallengeMemberStatus = ONGOING 또는 NOT_YET)
  - `SUCCESS`: 달성 (ChallengeMemberStatus = SUCCESS)
  - `FAIL`: 실패 (ChallengeMemberStatus = FAIL)
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**설명**
- 내가 참여한 챌린지를 상태별로 조회
- 참여일(createdAt) 내림차순 정렬

---

### 3.6 추천 챌린지 조회
```http
GET /challenge/recommended?size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `size`: 조회할 챌린지 개수 (기본값: 10)

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": [
    {
      "challengeId": 1,
      "title": "텀블러 사용 챌린지",
      "category": "TUMBLER",
      "startDate": "2024-04-01",
      "endDate": "2024-04-07",
      "countLikes": 350,
      "attendeeCount": 120
    }
  ]
}
```

**설명**
- 현재 모집 중인 챌린지 중 좋아요가 많은 순으로 조회
- 좋아요 내림차순, 시작일 오름차순 정렬
- 페이지네이션 없음

---

### 3.7 챌린지 상세 조회
```http
GET /challenge/{challengeId}
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `challengeId`: 챌린지 ID

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "challengeId": 1,
    "title": "텀블러 사용 챌린지",
    "content": "일주일간 텀블러 사용하기",
    "category": "TUMBLER",
    "startDate": "2024-04-01",
    "endDate": "2024-04-07",
    "status": "NOT_YET",
    "goalCount": 7,
    "attendeeCount": 45,
    "countLikes": 120,
    "isLiked": false,
    "isJoined": false,
    "infoImageUrls": ["https://...", "https://..."],
    "certImageUrls": ["https://...", "https://..."],
    "createdAt": "2024-03-20T10:00:00"
  }
}
```

---

### 3.8 챌린지 참여
```http
POST /challenge/{challengeId}/join
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `challengeId`: 참여할 챌린지 ID

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "challengeId": 1,
    "isJoined": true
  }
}
```

---

### 3.9 챌린지 찜하기
```http
POST /challenge/{challengeId}/like
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

---

### 3.10 챌린지 찜 취소
```http
DELETE /challenge/{challengeId}/like
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다."
}
```

---

### 3.11 모집 마감 챌린지 검색
```http
GET /challenge/search/closed?keyword=텀블러&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `keyword`: 검색 키워드 (제목 검색)
- `page`: 페이지 번호
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**설명**
- 시작일이 오늘 이전인 챌린지를 제목으로 검색
- 오늘 날짜에 가까운 순 정렬 (startDate 내림차순)

---

### 3.12 모집 중인 챌린지 검색
```http
GET /challenge/search/open?keyword=플로깅&sortType=DEADLINE_SOON&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `keyword`: 검색 키워드 (제목 검색)
- `sortType`: 정렬 기준 (DEADLINE_SOON: 마감임박순, NEWEST: 최신순) - 기본값: DEADLINE_SOON
- `page`: 페이지 번호
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

**설명**
- 시작일이 오늘 이후인 챌린지를 제목으로 검색
- 마감임박순 또는 최신순 정렬

---

## 4. 피드 API

### 4.1 피드 목록 조회
```http
GET /feed?category=ALL&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `category`: 카테고리 필터 (ALL, TUMBLER, PLOGGING 등)
- `page`: 페이지 번호
- `size`: 페이지 크기

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [
      {
        "feedId": 1,
        "memberId": 10,
        "memberNickname": "홍길동",
        "memberProfileUrl": "https://...",
        "challengeId": 5,
        "challengeTitle": "텀블러 사용 챌린지",
        "category": "TUMBLER",
        "content": "오늘도 텀블러 사용 성공!",
        "imageUrl": "https://...",
        "likeCount": 15,
        "isLiked": false,
        "createdAt": "2024-03-20T14:30:00"
      }
    ],
    "hasNext": true,
    "page": 0,
    "size": 10
  }
}
```

---

### 4.2 피드 상세 조회
```http
GET /feed/{feedId}
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `feedId`: 피드 ID

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "feedId": 1,
    "memberId": 10,
    "memberNickname": "홍길동",
    "memberProfileUrl": "https://...",
    "challengeId": 5,
    "challengeTitle": "텀블러 사용 챌린지",
    "category": "TUMBLER",
    "content": "오늘도 텀블러 사용 성공!",
    "imageUrl": "https://...",
    "likeCount": 15,
    "isLiked": false,
    "createdAt": "2024-03-20T14:30:00"
  }
}
```

---

### 4.3 피드 작성
```http
POST /feed
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data
```

**Request Body (multipart/form-data)**
```
data: {
  "challengeId": 5,
  "content": "오늘도 텀블러 사용 성공!"
}
feedImage: File (optional)
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "feedId": 123
  }
}
```

**주의사항**
- 챌린지 참여자만 피드 작성 가능
- 이미지는 선택 사항

---

### 4.4 피드 좋아요 추가
```http
POST /feed/{feedId}/like
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `feedId`: 피드 ID

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "feedId": 1,
    "likeCount": 16,
    "isLiked": true
  }
}
```

---

### 4.5 피드 좋아요 취소
```http
DELETE /feed/{feedId}/like
Authorization: Bearer {accessToken}
```

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "feedId": 1,
    "likeCount": 15,
    "isLiked": false
  }
}
```

---

### 4.6 내 피드 목록 조회
```http
GET /feed/my?category=ALL&page=0&size=4
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `category`: 카테고리 필터 (기본값: ALL)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 4, 최소: 1, 최대: 10)

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "hasNext": false,
    "page": 0,
    "size": 4
  }
}
```

---

### 4.7 피드 검색
```http
GET /feed/search?keyword=텀블러&category=ALL&page=0&size=10
Authorization: Bearer {accessToken}
```

**Query Parameters**
- `keyword`: 검색 키워드 (피드 내용 검색)
- `category`: 카테고리 필터 (기본값: ALL)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 10)

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "hasNext": false,
    "page": 0,
    "size": 10
  }
}
```

**설명**
- 피드 내용(review)을 키워드로 검색
- 등록 시간 기준으로 정렬

---

## 5. 발견(Achievement) API

### 5.1 명예의 전당 조회
```http
GET /api/achievement/hall-of-fame/{category}
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `category`: 카테고리 (ALL, TUMBLER, PLOGGING 등)

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "hallOfFameList": [
      {
        "rank": 1,
        "challengeId": 5,
        "challengeTitle": "텀블러 사용 챌린지",
        "likeCount": 350,
        "thumbnailUrl": "https://..."
      }
    ]
  }
}
```

**설명**
- 카테고리별 좋아요 TOP 10 챌린지 조회
- 순위는 1위부터 10위까지

---

### 5.2 카테고리별 피드 조회
```http
GET /api/achievement/feeds/{category}?page=0&size=4
Authorization: Bearer {accessToken}
```

**Path Parameters**
- `category`: 카테고리

**Query Parameters**
- `page`: 페이지 번호
- `size`: 페이지 크기 (기본값: 4)

**Response**
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {
    "content": [],
    "hasNext": false,
    "page": 0,
    "size": 4
  }
}
```

---

## 공통 응답 형식

### 성공 응답
```json
{
  "isSuccess": true,
  "code": "200",
  "message": "요청에 성공하였습니다.",
  "result": {}
}
```

### 실패 응답
```json
{
  "isSuccess": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

---

## 에러 코드

### 인증 관련 (4xx)
| 코드 | 메시지 | 설명 |
|-----|--------|------|
| `INVALID_JWT` | 유효하지 않은 토큰입니다 | JWT 토큰이 유효하지 않음 |
| `EXPIRED_JWT` | 만료된 토큰입니다 | JWT 토큰이 만료됨 |
| `INVALID_REFRESH_TOKEN` | 유효하지 않은 Refresh Token입니다 | Refresh Token이 유효하지 않음 |

### 회원 관련 (4xx)
| 코드 | 메시지 | 설명 |
|-----|--------|------|
| `NOT_FOUND_MEMBER` | 회원을 찾을 수 없습니다 | 존재하지 않는 회원 |
| `DUPLICATE_NICKNAME` | 이미 사용 중인 닉네임입니다 | 닉네임 중복 |
| `INVALID_NICKNAME_FORMAT` | 닉네임 형식이 올바르지 않습니다 | 2-10자 한글/영문/숫자 |
| `INVALID_PROVIDER` | 잘못된 소셜 로그인 제공자입니다 | Provider 불일치 |

### 챌린지 관련 (4xx)
| 코드 | 메시지 | 설명 |
|-----|--------|------|
| `NOT_FOUND_CHALLENGE` | 챌린지를 찾을 수 없습니다 | 존재하지 않는 챌린지 |
| `ALREADY_JOINED_CHALLENGE` | 이미 참여한 챌린지입니다 | 중복 참여 |
| `CHALLENGE_FULL` | 챌린지 참여 인원이 초과되었습니다 | 정원 초과 |

### 피드 관련 (4xx)
| 코드 | 메시지 | 설명 |
|-----|--------|------|
| `NOT_FOUND_FEED` | 피드를 찾을 수 없습니다 | 존재하지 않는 피드 |
| `NOT_CHALLENGE_MEMBER` | 챌린지 참여자가 아닙니다 | 미참여자 피드 작성 시도 |
| `ALREADY_LIKED_FEED` | 이미 좋아요한 피드입니다 | 중복 좋아요 |

### 서버 오류 (5xx)
| 코드 | 메시지 | 설명 |
|-----|--------|------|
| `INTERNAL_SERVER_ERROR` | 서버 오류가 발생했습니다 | 내부 서버 오류 |
| `KAKAO_LOGOUT_FAILED` | 카카오 로그아웃에 실패했습니다 | 카카오 API 오류 |
| `KAKAO_UNLINK_FAILED` | 카카오 연동 해제에 실패했습니다 | 카카오 API 오류 |
| `APPLE_REVOKE_FAILED` | 애플 연동 해제에 실패했습니다 | Apple API 오류 |

---

## 📝 참고사항

### 날짜/시간 형식
- 날짜: `yyyy-MM-dd` (예: 2024-03-20)
- 날짜/시간: `yyyy-MM-dd'T'HH:mm:ss` (예: 2024-03-20T14:30:00)

### 페이지네이션
- **PageResponseDTO**: 전체 페이지 정보 포함 (총 개수, 총 페이지)
  - 챌린지 목록 등에 사용
- **SliceResponseDTO**: 다음 페이지 존재 여부만 포함 (무한 스크롤)
  - 피드 목록 등에 사용

### 이미지 업로드
- Content-Type: `multipart/form-data`
- 지원 형식: JPEG, PNG
- 최대 크기: 3MB (파일당), 15MB (전체 요청)

### BaseResponse 구조
모든 API는 `BaseResponse<T>` 형식으로 응답합니다:
```json
{
  "isSuccess": boolean,
  "code": "string",
  "message": "string",
  "result": T
}
```

### 챌린지 상태 (ChallengeStatus)
- `NOT_YET`: 시작 전
- `IN_PROGRESS`: 진행 중
- `END`: 종료

### 챌린지 멤버 상태 (ChallengeMemberStatus)
- `NOT_YET`: 시작 전
- `ONGOING`: 진행 중
- `SUCCESS`: 성공
- `FAIL`: 실패

### 참여 상태 (ParticipationStatus)
- `ONGOING`: 참여 중 (NOT_YET + ONGOING)
- `SUCCESS`: 달성
- `FAIL`: 실패
