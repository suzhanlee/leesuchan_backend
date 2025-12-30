plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-mysql")
    runtimeOnly("com.mysql:mysql-connector-j")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
