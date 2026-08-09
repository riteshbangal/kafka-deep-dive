# Java Fundamentals

This directory is an independently buildable Gradle project for plain Java Kafka fundamentals examples.

The current project is a very small producer and consumer practice application that connects to a remote Kafka cluster, such as Aiven for Apache Kafka. It does not start Kafka locally and does not use Docker Compose, Kubernetes, Spring Boot, or a local Kafka container.

## Prerequisites

- JDK 17
- Gradle Wrapper from this directory (`./gradlew`)
- Access to a remote Kafka cluster
- Kafka authentication details from the remote provider

Gradle uses Java Toolchains and requests Java 17 for compilation. IntelliJ IDEA should import this directory from Gradle rather than from committed IDE metadata.

## Configuration

The application reads Kafka settings from a local `.env` file in this directory. The `.env` file is ignored by Git so real Aiven credentials are not committed.

Create it from the committed sample:

```bash
cp sample.env .env
```

Then edit `.env` with your Aiven Kafka values.

Required:

```dotenv
KAFKA_BOOTSTRAP_SERVERS=your-aiven-host:port
```

Optional:

```dotenv
KAFKA_TOPIC=practice.hello
KAFKA_CLIENT_ID=java-kafka-practice
KAFKA_GROUP_ID=java-practice-consumer
```

For SASL authentication, configure the Kafka Java client properties:

```dotenv
KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_JAAS_CONFIG=org.apache.kafka.common.security.plain.PlainLoginModule required username="avnadmin" password="your-password";
```

If your Aiven service uses client certificates or truststores instead, configure the relevant Java client SSL properties in `.env`:

```dotenv
KAFKA_SECURITY_PROTOCOL=SSL
KAFKA_SSL_TRUSTSTORE_LOCATION=/path/to/client.truststore.jks
KAFKA_SSL_TRUSTSTORE_PASSWORD=<your-store-pass>
KAFKA_SSL_KEYSTORE_LOCATION=/path/to/client.keystore.p12
KAFKA_SSL_KEYSTORE_PASSWORD=<your-store-pass>
KAFKA_SSL_KEY_PASSWORD=<your-store-pass>
```

Process environment variables take precedence over values from `.env`. If `.env` does not exist, the application prints a warning and continues with process environment variables and defaults.

Do not commit credentials, certificates, truststores, keystores, or local environment files.

## Build

From this directory:

```bash
./gradlew build
```

From the repository root composite build:

```bash
./gradlew build
```

## Run

Send one message:

```bash
./gradlew runProducer --args="Hello from Java"
```

Consume messages:

```bash
./gradlew runConsumer
```
