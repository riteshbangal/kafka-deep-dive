# Python Fundamentals

This directory contains Python examples for Kafka fundamentals.

## StreamStore Hello World

The current example is a small Kafka producer and consumer application in the `streamstore` package.

It uses:

- Python 3.10 or newer
- `confluent-kafka`
- uv, or plain `pip` with `requirements.txt`

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

The example defaults to:

- Bootstrap servers: `localhost:9092`
- Topic: `streamstore.hello`

Start the local Kafka broker from the repository root before running the producer or consumer:

```bash
docker compose -f docker/docker-compose.yml up -d
```

Override these values with environment variables:

```bash
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_TOPIC=streamstore.hello
```

The producer and consumer create the topic if it does not already exist.

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
