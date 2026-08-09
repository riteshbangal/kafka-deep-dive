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
│   └── python/           # Python fundamentals examples
├── docker/               # Docker and Docker Compose environments
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

This repository currently contains the initial documentation structure and one Java fundamentals Gradle project. Kafka examples, local environments, and application projects will be added progressively.

Start with the root README, then use the README in each directory to understand what belongs there as content is added.

## Java and Gradle Workflow

Open the repository root in IntelliJ IDEA. IntelliJ can import the root `settings.gradle.kts`, which registers Java projects as Gradle included builds. This keeps examples independently buildable while still making them discoverable from the repository root.

The current Java project is:

- `fundamentals/java` (`kafka-fundamentals-java`)

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
