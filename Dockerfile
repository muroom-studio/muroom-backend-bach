# STAGE 1: Build Stage - 애플리케이션 빌드 단계
# 1. 빌드 환경의 기반 이미지로 Java 21 JDK가 포함된 Amazon Corretto를 사용.
#    이 스테이지를 'builder'라는 별칭으로 명명.
FROM amazoncorretto:21 AS builder

# 2. 컨테이너 내부 작업 디렉토리를 /app으로 지정.
WORKDIR /app

# 3. 빌드 캐시 최적화를 위해 의존성과 관련된 파일들을 먼저 복사.
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 4. 소스 코드를 복사. 이 부분의 변경이 가장 잦으므로, 캐시 효율을 위해 나중에 복사.
COPY src ./src

# 5. Gradle을 사용하여 애플리케이션을 빌드하고, 테스트는 제외.
RUN ./gradlew build -x test


# STAGE 2: Runtime Stage - 최적화된 최종 실행 단계
# 6. 최종 실행 환경의 기반 이미지로 경량화된 Alpine Linux 기반의 Corretto JRE 이미지를 사용.
#    Full JDK 버전 대비 이미지 크기가 훨씬 작아 효율적.
FROM amazoncorretto:21-alpine

# 7. 컨테이너 내부 작업 디렉토리를 /app으로 지정.
WORKDIR /app

# 8. 보안 강화를 위해 애플리케이션 실행 전용 그룹(muroom)과 사용자(monte)를 생성.
RUN addgroup -S muroom && adduser -S monte -G muroom

# 9. 이전 'builder' 스테이지에서 생성된 .jar 파일만 현재 스테이지로 복사.
#    복사된 파일의 소유자를 방금 생성한 monte:muroom로 변경.
COPY --from=builder --chown=monte:muroom /app/build/libs/*.jar muroom-backend-bach.jar

# 10. 컨테이너의 기본 사용자를 monte로 전환.
#     이후 명령어는 root가 아닌 monte 권한으로 실행됨.
USER monte

# 11. 컨테이너가 8080 포트를 리슨할 것임을 명시.
EXPOSE 8080

# 12. 컨테이너 시작 시 monte 권한으로 애플리케이션을 실행.
ENTRYPOINT ["java", "-jar", "muroom-backend-bach.jar"]