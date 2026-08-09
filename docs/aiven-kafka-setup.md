# Aiven Kafka Setup for `kafka-deep-dive`

This guide explains how to create a free-tier Aiven for Apache Kafka cluster, configure it for the learning examples, and connect the Java practice project in `fundamentals/java`.

> Never commit real Kafka credentials, passwords, certificates, truststores, keystores, or `.env` files.

## Goal

The Java fundamentals project connects to Kafka hosted remotely on Aiven. This keeps the Java example focused on producer, consumer, topic, partition, offset, and consumer-group behavior without adding Docker Compose, Kubernetes, Spring Boot, or a local Kafka broker.

```text
fundamentals/java
├── ProducerApp
├── ConsumerApp
└── KafkaConfig
        │
        │ SASL_SSL
        ▼
Aiven for Apache Kafka
        │
        └── kafka-deep-dive.basics
```

## Create the Aiven Kafka Cluster

1. Open <https://console.aiven.io/>.
2. Create or select an Aiven project.
3. Create a new service.
4. Choose **Apache Kafka**.
5. Select the free-tier or lowest-cost development option available for your account and region.
6. Choose the cloud provider and region closest to you.
7. Create the service and wait until it reaches the running state.

After the service is running, open the service overview and collect:

- Kafka bootstrap host and port
- Username, usually `avnadmin`
- Password
- CA certificate
- Authentication/security details

For this project, the Java client uses:

```text
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
```

## Create the Topic

Create this topic in the Aiven console:

```text
kafka-deep-dive.basics
```

Use a small learning-friendly configuration:

```text
Partitions:          2
Replication factor:  2, if the free-tier cluster has 2 Kafka nodes
Minimum ISR:         1
Cleanup policy:      delete
Retention:           168 hours
```

If Aiven reports that the replication factor cannot exceed the number of Kafka nodes, lower the replication factor to match the cluster size. For a single-node free-tier service, use replication factor `1`.

Two partitions are useful because they let you run two consumers in the same group and observe partition assignment.

## Prepare TLS Trust

Download the Aiven CA certificate from the service connection page.

Store local certificate material outside Git tracking, for example:

```text
certificates/ca.pem
```

Create a Java truststore:

```bash
keytool \
  -importcert \
  -alias aiven-ca \
  -file certificates/ca.pem \
  -keystore certificates/aiven-truststore.jks \
  -storepass "<your-store-pass>" \
  -noprompt
```

The Java Kafka client will use:

```text
certificates/aiven-truststore.jks
```

The truststore password should be your own local value. Do not use a real password in committed documentation or sample files.

## Configure the Java Project

Move into the Java project:

```bash
cd fundamentals/java
```

Create a local `.env` from the committed sample:

```bash
cp sample.env .env
```

Edit `.env` with your Aiven values:

```dotenv
# ============================================
# Aiven Kafka connection
# ============================================

KAFKA_BOOTSTRAP_SERVERS=<your-aiven-host>:<your-aiven-port>

KAFKA_SECURITY_PROTOCOL=SASL_SSL
KAFKA_SASL_MECHANISM=PLAIN
KAFKA_SASL_JAAS_CONFIG='org.apache.kafka.common.security.plain.PlainLoginModule required username="avnadmin" password="<your-aiven-password>";'

# ============================================
# TLS trust
# ============================================

KAFKA_SSL_TRUSTSTORE_LOCATION=/absolute/path/to/kafka-deep-dive/certificates/aiven-truststore.jks
KAFKA_SSL_TRUSTSTORE_PASSWORD=<your-store-pass>

# ============================================
# Kafka application
# ============================================

KAFKA_TOPIC=kafka-deep-dive.basics
KAFKA_CLIENT_ID=kafka-deep-dive-aiven
KAFKA_GROUP_ID=java-practice-consumer
```

`KafkaConfig` loads this `.env` file from the current working directory. Run Gradle commands from `fundamentals/java` so `.env` is found.

If `.env` is missing, the application prints a warning and continues with process environment variables and defaults. It still requires `KAFKA_BOOTSTRAP_SERVERS` before it can connect to Kafka.

## Build the Java Project

From `fundamentals/java`:

```bash
./gradlew build
```

The project uses:

- Plain Java
- Gradle Kotlin DSL
- Java 17 toolchain
- Official Apache Kafka Java client

## Run the Producer

Send one message:

```bash
./gradlew runProducer --args="Hello from Java"
```

Successful output looks like:

```text
Produced topic=kafka-deep-dive.basics partition=0 offset=0 value="Hello from Java"
```

## Verify the Message in Aiven

In <https://console.aiven.io/>:

1. Open the Kafka service.
2. Go to **Topics**.
3. Open `kafka-deep-dive.basics`.
4. Open the messages view.
5. Fetch messages from offset `0`.

The important Kafka metadata to observe is:

```text
topic
partition
offset
key
value
```

## Run the Consumer

Start the consumer:

```bash
./gradlew runConsumer
```

The consumer uses:

```text
group.id=java-practice-consumer
auto.offset.reset=earliest
enable.auto.commit=true
```

Example consumed record:

```text
Consumed topic=kafka-deep-dive.basics partition=0 offset=0 key=<uuid> value="Hello from Java"
```

With two partitions, a single consumer receives both partitions. If you later run two consumers with the same group ID, Kafka can assign one partition to each consumer.

## Common Errors

### `SSL handshake failed`

This usually means Java does not trust the Aiven CA certificate.

Check:

- The CA certificate was downloaded from Aiven.
- The certificate was imported into the truststore.
- `KAFKA_SSL_TRUSTSTORE_LOCATION` points to the correct truststore.
- `KAFKA_SSL_TRUSTSTORE_PASSWORD` matches `<your-store-pass>`.

### `UNKNOWN_TOPIC_OR_PARTITION`

This means the client connected to Kafka, but the requested topic does not exist.

Check:

- The topic exists in Aiven.
- `KAFKA_TOPIC=kafka-deep-dive.basics` is set in `.env`.
- You are running Gradle from `fundamentals/java`.

### Missing `KAFKA_BOOTSTRAP_SERVERS`

The Java app needs the Aiven bootstrap server before it can connect.

Set it in `fundamentals/java/.env`:

```dotenv
KAFKA_BOOTSTRAP_SERVERS=<your-aiven-host>:<your-aiven-port>
```

## Security Checklist

Before pushing to GitHub:

```text
[ ] .env is ignored
[ ] real Kafka passwords are not committed
[ ] certificates/ is ignored
[ ] *.jks, *.p12, *.pem, and *.key files are ignored
[ ] sample.env contains placeholders only
[ ] README files contain placeholders only
```
