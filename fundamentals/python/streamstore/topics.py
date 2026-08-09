from confluent_kafka.admin import AdminClient, NewTopic


def ensure_topic(bootstrap_servers: str, topic_name: str) -> None:
    admin = AdminClient({"bootstrap.servers": bootstrap_servers})
    metadata = admin.list_topics(timeout=10)
    if topic_name in metadata.topics:
        return

    futures = admin.create_topics(
        [NewTopic(topic_name, num_partitions=1, replication_factor=1)]
    )
    try:
        futures[topic_name].result(timeout=10)
    except Exception as error:
        if "Topic already exists" not in str(error):
            raise
