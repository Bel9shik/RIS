import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.testing.Test

group = "nsu.kardash"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "java")
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
