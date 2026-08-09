package dev.deepdive.kafka.practice;

import dev.deepdive.kafka.config.KafkaConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public final class ProducerApp {

    private static final Logger log = LoggerFactory.getLogger(ProducerApp.class.getSimpleName());

    private ProducerApp() {
        log.info("I am a Kafka Producer!");
    }

    public static void main(String[] args) throws Exception {
        var topic = KafkaConfig.topic();
        var message = args.length == 0 ? "Hello from Java Kafka practice at " + Instant.now() : String.join(" ", args);

        // Load the common Kafka properties and set the producer-specific properties
        var props = KafkaConfig.commonProperties();
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        try (var producer = new KafkaProducer<String, String>(props)) {
            var record = new ProducerRecord<>(topic, UUID.randomUUID().toString(), message);
            var metadata = producer.send(record).get();
            System.out.printf(
                    "Produced topic=%s partition=%d offset=%d value=\"%s\"%n",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    message
            );
        }
    }
}
