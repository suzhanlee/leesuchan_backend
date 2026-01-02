# 빌드 스테이지
FROM gradle:8.13-jdk17 AS builder
WORKDIR /app

# gradlew 실행 권한 설정 및 의존성 캐싱
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle account build.gradle.kts ./account/
COPY --chown=gradle:gradle activity build.gradle.kts ./activity/
COPY --chown=gradle:gradle common build.gradle.kts ./common/
COPY --chown=gradle:gradle service build.gradle.kts ./service/
COPY --chown=gradle:gradle infra build.gradle.kts ./infra/

RUN gradle --version

# 소스 코드 복사 후 빌드
COPY --chown=gradle:gradle . .
RUN gradle :service:assemble -x test --no-daemon

# 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 비루트 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 결과물 복사
COPY --from=builder /app/service/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# JVM 옵션 및 실행
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar"]
