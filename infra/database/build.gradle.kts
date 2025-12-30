dependencies {
    implementation(project(":account"))
    implementation(project(":activity"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.mysql:mysql-connector-j")
}
