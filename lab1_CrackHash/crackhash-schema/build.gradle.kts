plugins {
    id("java-library")
}

java {
    toolchain {
        languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(17))
    }
}

dependencies {
    api("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    api("org.glassfish.jaxb:jaxb-runtime:4.0.5")
}
