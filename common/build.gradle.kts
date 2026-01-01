plugins {
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework:spring-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
