# Multi-stage build를 사용하여 빌드 시간 최적화
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# Gradle 의존성 파일들을 먼저 복사하여 캐시 활용
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# 의존성 다운로드 (캐싱을 위해 소스 코드 복사 전에 실행)
RUN gradle build --no-daemon -x test || true

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle bootJar --no-daemon

# 실행 이미지 생성
FROM openjdk:17-jdk-alpine

WORKDIR /app

# 애플리케이션 실행을 위한 사용자 생성 (보안)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown appuser:appgroup app.jar

# 비root 사용자로 변경
USER appuser

# 포트 노출
EXPOSE 8080

# JVM 옵션 설정
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]