from datetime import datetime, timezone
from sys import argv
from uuid import uuid4

from confluent_kafka import Producer

from streamstore.config import bootstrap_servers, topic
from streamstore.topics import ensure_topic


def delivery_report(error, message) -> None:
    if error is not None:
        print(f"Failed to produce message: {error}")
        return

    print(
        "Produced "
        f"topic={message.topic()} "
        f"partition={message.partition()} "
        f"offset={message.offset()} "
        f'key={message.key().decode("utf-8")} '
        f'value="{message.value().decode("utf-8")}"'
    )


def main() -> None:
    servers = bootstrap_servers()
    kafka_topic = topic()
    message = " ".join(argv[1:]) if len(argv) > 1 else "Hello World from StreamStore"
    value = f"{message} at {datetime.now(timezone.utc).isoformat()}"
    key = str(uuid4())

    ensure_topic(servers, kafka_topic)

    producer = Producer(
        {
            "bootstrap.servers": servers,
            "client.id": "streamstore-hello-world-producer",
            "acks": "all",
        }
    )

    producer.produce(
        kafka_topic,
        key=key.encode("utf-8"),
        value=value.encode("utf-8"),
        callback=delivery_report,
    )
    producer.flush(timeout=10)


if __name__ == "__main__":
    main()
