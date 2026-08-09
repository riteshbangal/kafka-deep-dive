plugins {
  application
  java
}

group = "dev.riteshbangal.kafka"
version = "0.1.0-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

dependencies {
  implementation("org.apache.kafka:kafka-clients:4.1.1")
  implementation("org.slf4j:slf4j-simple:2.0.17")
}

application {
  mainClass = "dev.riteshbangal.kafka.practice.ProducerApp"
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(17)
}

tasks.register<JavaExec>("runProducer") {
  group = "application"
  description = "Sends one message to the configured remote Kafka topic."
  classpath = sourceSets.main.get().runtimeClasspath
  mainClass = "dev.riteshbangal.kafka.practice.ProducerApp"
}

tasks.register<JavaExec>("runConsumer") {
  group = "application"
  description = "Consumes messages from the configured remote Kafka topic."
  classpath = sourceSets.main.get().runtimeClasspath
  mainClass = "dev.riteshbangal.kafka.practice.ConsumerApp"
}
