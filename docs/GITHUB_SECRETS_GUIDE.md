# GitHub Secrets 설정 가이드

## GitHub Repository Settings에서 설정할 Secrets

### 공통 환경변수

- `JWT_SECRET_KEY`: JWT 토큰 암호화 키
- `KAKAO_CLIENT_ID`: 카카오 OAuth 클라이언트 ID
- `KAKAO_CLIENT_SECRET`: 카카오 OAuth 클라이언트 시크릿
- `KAKAO_REDIRECT_URI`: 카카오 OAuth 리다이렉트 URI
- `AWS_ACCESS_KEY_ID`: AWS 액세스 키 ID
- `AWS_SECRET_ACCESS_KEY`: AWS 시크릿 액세스 키

### Stage 서버 관련

- `STAGE_HOST`: Stage 서버 IP 또는 도메인
- `STAGE_USER`: Stage 서버 SSH 사용자명
- `STAGE_SSH_KEY`: Stage 서버 SSH 개인키 (PEM 형식)
- `STAGE_DB_URL`: Stage 데이터베이스 URL
- `STAGE_DB_USER`: Stage 데이터베이스 사용자명
- `STAGE_DB_PASSWORD`: Stage 데이터베이스 비밀번호

### Production 서버 관련

- `PROD_HOST`: Production 서버 IP 또는 도메인
- `PROD_USER`: Production 서버 SSH 사용자명
- `PROD_SSH_KEY`: Production 서버 SSH 개인키 (PEM 형식)
- `PROD_DB_URL`: Production 데이터베이스 URL
- `PROD_DB_USER`: Production 데이터베이스 사용자명
- `PROD_DB_PASSWORD`: Production 데이터베이스 비밀번호

## 설정 방법

1. GitHub 저장소 → Settings → Secrets and variables → Actions
2. "New repository secret" 클릭
3. 위의 Secret 이름과 값을 각각 추가

## SSH 키 생성 방법

```bash
# SSH 키 페어 생성
ssh-keygen -t rsa -b 4096 -f ~/.ssh/deploy_key

# 공개키를 서버에 등록
ssh-copy-id -i ~/.ssh/deploy_key.pub user@server-ip

# 개인키 내용을 GitHub Secret에 등록
cat ~/.ssh/deploy_key
```

## 서버 사전 준비사항

### Docker 설치

```bash
sudo apt update
sudo apt install docker.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER
```

### 배포 디렉토리 생성

```bash
mkdir -p /home/$USER/deploy
```
