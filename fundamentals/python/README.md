# Python Fundamentals

This directory contains Python examples for Kafka fundamentals.

## StreamStore Hello World

The current project is a small Kafka producer and consumer application in the `streamstore` package.

Use [python-hello-world.md](python-hello-world.md) for the Kafka flow, runtime behavior, and configuration details for this example.

## Technology

- Python 3.10 or newer
- `confluent-kafka`
- uv, or `pip` with `requirements.txt`

## Setup

From this directory, install dependencies with uv:

```bash
uv sync
```

Or with pip:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Kafka Configuration

The example expects an unsecured Kafka broker to be reachable before running the producer or consumer.

Defaults:

- Bootstrap servers: `localhost:9092`
- Topic: `streamstore.hello`

Override these values with environment variables:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC=streamstore.hello
```

The repository Docker setup may be used as one way to provide Kafka locally. See [../../docker/README.md](../../docker/README.md) for Docker-related notes.

## Run

Start the consumer:

```bash
uv run python -m streamstore.consumer
```

Produce a message:

```bash
uv run python -m streamstore.producer "Hello from Kafka Deep Dive"
```

If you are using an activated virtual environment instead of uv, omit `uv run`:

```bash
python -m streamstore.consumer
python -m streamstore.producer "Hello from Kafka Deep Dive"
```
