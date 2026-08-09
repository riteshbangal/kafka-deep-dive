# Kafka Deep Dive

Kafka Deep Dive is a long-term learning and reference repository for Apache Kafka. It is intended to grow into a practical collection of notes, runnable examples, local environments, Kubernetes manifests, and real-world streaming applications.

The repository is organized as a monorepo, but individual examples and applications should remain reasonably independent. Each project can add its own dependencies, README, build configuration, and runtime instructions when it is introduced.

## Repository Structure

```text
kafka-deep-dive/
├── settings.gradle.kts   # Root Gradle composite-build entry point
├── build.gradle.kts      # Root workspace tasks for included builds
├── gradlew               # Root Gradle Wrapper for repository-level Gradle tasks
├── gradlew.bat
├── gradle/               # Root Gradle Wrapper files
├── docs/                 # Concept notes, guides, and reference material
├── fundamentals/         # Small focused examples for Kafka concepts
│   ├── java/             # Independent Gradle project for Java fundamentals examples
│   └── python/           # Python fundamentals examples and uv project
├── docker/               # Docker Compose local Kafka environment
├── kubernetes/           # Kubernetes manifests and deployment notes
├── scripts/              # Repository helper scripts
└── .github/              # GitHub metadata and project automation
```

## Technology Stack

- Apache Kafka
- Java
- Gradle
- Gradle Java Toolchains
- Python
- uv
- Docker
- Docker Compose
- Kubernetes

## Getting Started

This repository currently contains the initial documentation structure, one Java fundamentals Gradle project, one Python fundamentals Kafka hello world example, and a Docker Compose local Kafka environment. Kafka examples and application projects will be added progressively.

Start with the root README, then use the README in each directory to understand what belongs there as content is added.

## Java and Gradle Workflow

Open the repository root in IntelliJ IDEA. IntelliJ can import the root `settings.gradle.kts`, which registers Java projects as Gradle included builds. This keeps examples independently buildable while still making them discoverable from the repository root.

The current Java project is:

- `fundamentals/java` (`kafka-fundamentals-java`)

It is a plain Java Kafka producer/consumer practice project using the official Apache Kafka Java client. It is intended to connect to a remote Kafka cluster such as Aiven, not to a local Kafka container.

Build it from the repository root:

```bash
./gradlew build
```

Build it independently from its own directory:

```bash
cd fundamentals/java
./gradlew build
```

Java versions are configured with Gradle Java Toolchains. Java 21 is the default toolchain for Java projects unless an example documents a different requirement. Dependencies should come from Maven Central.

Do not commit IntelliJ project metadata such as `.idea/` or `*.iml`. IntelliJ configuration should be derived from Gradle.

Configure the Java example with a local `.env` file before running it:

```bash
cd fundamentals/java
cp sample.env .env
```

Edit `.env` with your Aiven Kafka bootstrap server and credentials. Then run it from `fundamentals/java`:

```bash
./gradlew runProducer --args="Hello from Java"
./gradlew runConsumer
```

## Python Workflow

The current Python example is:

- `fundamentals/python` (`streamstore`)

Install dependencies from the Python project directory:

```bash
cd fundamentals/python
uv sync
```

Run the Kafka hello world consumer and producer:

```bash
uv run python -m streamstore.consumer
uv run python -m streamstore.producer "Hello from Kafka Deep Dive"
```

The example expects Kafka at `localhost:9092` by default and uses topic `streamstore.hello`. Override these with `KAFKA_BOOTSTRAP_SERVERS` and `KAFKA_TOPIC`.

## Local Kafka

Start Kafka and Kafka UI with Docker Compose:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Endpoints:

- Kafka broker: `localhost:9092`
- Kafka UI: <http://localhost:8080>

Stop the local environment:

```bash
docker compose -f docker/docker-compose.yml down
```

## Roadmap

Planned coverage includes:

- Kafka fundamentals and core architecture
- Producers and consumers
- Serialization and schema evolution
- Topics, partitions, and replication
- Offsets, commits, and consumer lag
- Consumer groups and rebalancing
- Delivery semantics and error handling
- Docker-based local Kafka environments
- Kubernetes deployment patterns
- Real-world stream-processing applications

## Project Principles

- Keep examples focused and easy to run.
- Prefer clear documentation over hidden conventions.
- Avoid shared abstractions until repeated patterns justify them.
- Let each application own its dependencies and setup.
- Add automation only when it supports real workflows.

## License

This repository is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.
