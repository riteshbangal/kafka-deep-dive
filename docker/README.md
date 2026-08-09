# Docker

This directory contains Dockerfiles, Docker Compose files, and local runtime environments for Kafka examples.

Keep environments documented and scoped to the examples or applications they support.

## Local Kafka

`docker-compose.yml` starts a single-node Kafka broker in KRaft mode and Kafka UI.

Services:

- Kafka broker: `localhost:9092`
- Kafka UI: <http://localhost:8080>

Start the stack from this directory:

```bash
docker compose up -d
```

Check service status:

```bash
docker compose ps
```

Stop the stack:

```bash
docker compose down
```

From the repository root, use:

```bash
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml down
```
