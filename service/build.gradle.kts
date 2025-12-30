plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":account"))
    implementation(project(":activity"))
    implementation(project(":common"))
    implementation(project(":infra:database"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}

// bootJar 비활성화 (아직 main class 없음)
tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
