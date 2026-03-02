.PHONY: help stage-up stage-down stage-restart stage-logs stage-deploy prod-up prod-down prod-restart prod-logs prod-deploy ssl-init

# 도움말 (기본)
help:
	@echo "사용 가능한 명령어:"
	@echo "  make stage-up       - Stage 환경 시작"
	@echo "  make stage-down     - Stage 환경 중지"
	@echo "  make stage-restart  - Stage 환경 재시작"
	@echo "  make stage-logs     - Stage 환경 로그 보기"
	@echo "  make stage-deploy   - Stage 환경 배포"
	@echo "  make prod-up       - Prod 환경 시작"
	@echo "  make prod-down     - Prod 환경 중지"
	@echo "  make prod-restart  - Prod 환경 재시작"
	@echo "  make prod-logs     - Prod 환경 로그 보기"
	@echo "  make prod-deploy   - Prod 환경 배포"
	@echo "  make ssl-init      - SSL 인증서 발급"

# Stage 환경 시작
stage-up:
	@echo "🚀 Stage 환경 시작..."
	docker compose -f docker-compose.yml -f docker-compose.stage.yml up -d
	@echo "✅ 완료!"

# Stage 환경 중지
stage-down:
	@echo "🛑 Stage 환경 중지..."
	docker compose -f docker-compose.yml -f docker-compose.stage.yml down
	@echo "✅ 완료!"

# Stage 환경 재시작
stage-restart: stage-down stage-up

# Stage 환경 로그 보기
stage-logs:
	docker compose -f docker-compose.yml -f docker-compose.stage.yml logs -f

# Stage 환경 배포
stage-deploy:
	@echo "📦 Stage 이미지 배포 중..."
	docker compose -f docker-compose.stage.yml up -d beilsang-server
	@echo "✅ Stage 배포 완료!"

# Prod 환경 시작
prod-up:
	@echo "🚀 Prod 환경 시작..."
	docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
	@echo "✅ 완료!"

# Prod 환경 중지
prod-down:
	@echo "🛑 Prod 환경 중지..."
	docker compose -f docker-compose.yml -f docker-compose.prod.yml down
	@echo "✅ 완료!"

# Prod 환경 재시작
prod-restart: prod-down prod-up

# Prod 환경 로그 보기
prod-logs:
	docker compose -f docker-compose.yml -f docker-compose.prod.yml logs -f

# Prod 환경 배포
prod-deploy:
	@echo "📦 Prod 이미지 배포 중..."
	docker compose -f docker-compose.prod.yml up -d beilsang-server
	@echo "✅ Prod 배포 완료!"


# SSL 인증서 발급
ssl-init:
	@echo "🔒 SSL 인증서 발급 중..."
	docker compose up -d nginx
	docker compose run --rm certbot certonly \
		--webroot --webroot-path /var/www/certbot \
		--email yjs19990421@gmail.com \
		--agree-tos --no-eff-email \
		-d stage.beilsang.xyz
	docker compose restart nginx
	@echo "✅ 완료!"
