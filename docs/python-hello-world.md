# Python Hello World Sample

This document explains how to run the Python Kafka hello world sample in
`fundamentals/python`.

Kafka runs locally through `docker/docker-compose.yml`.

## Prerequisites

- Docker Desktop or Docker Engine with Docker Compose
- Python 3.10 or newer
- uv, or `pip` with `venv`

## Start Kafka

From the project root:

```bash
docker compose -f docker/docker-compose.yml up -d
```

This starts:

- Kafka broker on `localhost:9092`
- Kafka UI on `http://localhost:8080`

Check the containers:

```bash
docker compose -f docker/docker-compose.yml ps
```

## Sample Directory

Move into the Python sample directory before running Python commands:

```bash
cd fundamentals/python
```

## Install Python Dependencies

If you use `uv`, install the dependencies with:

```bash
uv sync
```

Then run the app with `uv run`, as shown below.

If you prefer `venv`, create and activate a virtual environment first:

```bash
python3 -m venv .venv
source .venv/bin/activate
```

Then install the Confluent Kafka client inside that virtual environment:

```bash
python -m pip install -r requirements.txt
```

## Run the Consumer

Open one terminal and run:

```bash
uv run python -m streamstore.consumer
```

If you activated a `venv`, you can use `python -m streamstore.consumer` instead.

The consumer listens on the default topic:

```text
streamstore.hello
```

The app creates this topic automatically if it does not already exist.

## Run the Producer

Open a second terminal and run:

```bash
uv run python -m streamstore.producer
```

To send a custom message:

```bash
uv run python -m streamstore.producer "Hello Kafka"
```

If you activated a `venv`, replace `uv run python` with `python`.

The consumer terminal should print the produced message.

## Configuration

The application uses these environment variables:

| Variable | Default | Purpose |
| --- | --- | --- |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `KAFKA_TOPIC` | `streamstore.hello` | Topic used by producer and consumer |

Example:

```bash
KAFKA_TOPIC=my-topic uv run python -m streamstore.producer
```

## Stop Kafka

```bash
docker compose -f docker/docker-compose.yml down
```

To remove Kafka data and containers:

```bash
docker compose -f docker/docker-compose.yml down -v
```
