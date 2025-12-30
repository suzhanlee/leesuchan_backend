plugins {
    java
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.leesuchan"
    version = "1.0.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.0")
        }
    }

    repositories {
        mavenCentral()
    }

    val lombok = "1.18.30"
    dependencies {
        compileOnly("org.projectlombok:lombok:${lombok}")
        annotationProcessor("org.projectlombok:lombok:${lombok}")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    tasks.test {
        useJUnitPlatform()
    }
}
