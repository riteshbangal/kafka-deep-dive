from confluent_kafka import Consumer, KafkaException

from streamstore.config import bootstrap_servers, topic
from streamstore.topics import ensure_topic


def main() -> None:
    servers = bootstrap_servers()
    kafka_topic = topic()
    ensure_topic(servers, kafka_topic)

    consumer = Consumer(
        {
            "bootstrap.servers": servers,
            "group.id": "streamstore-hello-world-consumer",
            "auto.offset.reset": "earliest",
            "enable.auto.commit": True,
        }
    )
    consumer.subscribe([kafka_topic])

    print(f"Listening for messages on topic {kafka_topic}. Press Ctrl+C to stop.")
    try:
        while True:
            record = consumer.poll(timeout=1.0)
            if record is None:
                continue
            if record.error():
                raise KafkaException(record.error())

            key = record.key().decode("utf-8") if record.key() else None
            value = record.value().decode("utf-8") if record.value() else None
            print(
                "Consumed "
                f"topic={record.topic()} "
                f"partition={record.partition()} "
                f"offset={record.offset()} "
                f"key={key} "
                f'value="{value}"'
            )
    except KeyboardInterrupt:
        print("Consumer stopped.")
    finally:
        consumer.close()


if __name__ == "__main__":
    main()
