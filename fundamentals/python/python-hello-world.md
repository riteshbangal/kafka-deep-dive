# Python Hello World Sample

This document explains how the Python Kafka hello world sample sends and receives records.

## Prerequisites

- Python 3.10 or newer
- uv, or `pip` with `venv`
- An unsecured single-node Kafka broker

The broker can come from any local or remote environment. The sample defaults to `localhost:9092`.

## Kafka Assumptions

This sample is intentionally small and assumes:

- One unsecured Kafka broker
- No TLS or SASL authentication
- One topic, `streamstore.hello` by default
- One partition
- Replication factor `1`

The producer and consumer create the topic if it does not already exist.

## Install Python Dependencies

Move into the Python fundamentals directory before running Python commands:

```bash
cd fundamentals/python
```

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

## How the Producer Works

The producer in `streamstore.producer`:

- Reads the Kafka bootstrap server and topic from configuration.
- Uses the command-line arguments as the message body, or a default hello-world message.
- Adds a UTC timestamp to the message value.
- Generates a UUID string as the record key.
- Ensures the topic exists before producing.
- Sends the record with `acks=all`.
- Flushes before exiting so the delivery callback can report the result.

When delivery succeeds, it prints the topic, partition, offset, key, and value returned by Kafka.

Run the producer:

```bash
uv run python -m streamstore.producer
```

To send a custom message:

```bash
uv run python -m streamstore.producer "Hello Kafka"
```

If you activated a `venv`, replace `uv run python` with `python`.

## How the Consumer Works

The consumer in `streamstore.consumer`:

- Reads the Kafka bootstrap server and topic from configuration.
- Ensures the topic exists before subscribing.
- Joins the consumer group `streamstore-hello-world-consumer`.
- Uses `auto.offset.reset=earliest`, so a new group starts with the earliest available record.
- Polls Kafka for records until stopped.
- Prints the topic, partition, offset, key, and value for each consumed record.
- Closes the consumer cleanly on `Ctrl+C`.

Open one terminal and run:

```bash
uv run python -m streamstore.consumer
```

If you activated a `venv`, you can use `python -m streamstore.consumer` instead.

Then run the producer from another terminal to send records.

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

The default topic is:

```text
streamstore.hello
```
