plugins {
  java
}

group = "dev.riteshbangal.kafka"
version = "0.1.0-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(21)
}
