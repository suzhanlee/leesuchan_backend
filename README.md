# leesuchan_backend

과제

## 실행 방법

### Docker Compose로 실행

```bash
# 1. Docker Compose로 인프라 서비스 실행 (MySQL)
docker-compose up -d

# 2. 애플리케이션 실행
./gradlew :service:bootRun
```

### API 접속

Swagger UI: http://localhost:8080/swagger-ui.html
