plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":account"))
    implementation(project(":activity"))
    implementation(project(":common"))
    implementation(project(":infra:database"))
    implementation(project(":infra:flyway"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
