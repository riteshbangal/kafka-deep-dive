package dev.riteshbangal.kafka.practice;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;

public final class ConsumerApp {
    private ConsumerApp() {
    }

    public static void main(String[] args) {
        var topic = KafkaConfig.topic();
        var props = KafkaConfig.commonProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfig.env("KAFKA_GROUP_ID", "java-practice-consumer"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");

        try (var consumer = new KafkaConsumer<String, String>(props)) {
            consumer.subscribe(List.of(topic));
            System.out.printf("Listening for messages on topic %s. Press Ctrl+C to stop.%n", topic);

            while (true) {
                var records = consumer.poll(Duration.ofSeconds(1));
                for (var record : records) {
                    System.out.printf(
                            "Consumed topic=%s partition=%d offset=%d key=%s value=\"%s\"%n",
                            record.topic(),
                            record.partition(),
                            record.offset(),
                            record.key(),
                            record.value()
                    );
                }
            }
        }
    }
}
