import os


DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092"
DEFAULT_TOPIC = "streamstore.hello"


def bootstrap_servers() -> str:
    return os.getenv("KAFKA_BOOTSTRAP_SERVERS", DEFAULT_BOOTSTRAP_SERVERS)


def topic() -> str:
    return os.getenv("KAFKA_TOPIC", DEFAULT_TOPIC)
