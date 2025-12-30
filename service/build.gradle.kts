plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":account"))
    implementation(project(":activity"))
    implementation(project(":common"))
    implementation(project(":infra:database"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
