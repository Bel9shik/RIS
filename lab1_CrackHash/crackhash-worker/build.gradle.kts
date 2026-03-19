plugins {
    id("java")
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
}

java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":crackhash-schema"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.dpaukov:combinatoricslib3:3.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("nsu.kardash.crackhash.worker.CrackHashWorkerApplication")
}
