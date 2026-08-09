package dev.deepdive.kafka.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public final class KafkaConfig {
    static final String DEFAULT_TOPIC = "practice.hello";
    private static final Path ENV_FILE = Path.of(".env");
    private static final Map<String, String> DOTENV = loadDotenv();

    private KafkaConfig() {
    }

    public static String topic() {
        return env("KAFKA_TOPIC", DEFAULT_TOPIC);
    }

    public static Properties commonProperties() {
        var props = new Properties();
        props.put("bootstrap.servers", requiredEnv("KAFKA_BOOTSTRAP_SERVERS"));
        props.put("client.id", env("KAFKA_CLIENT_ID", "kafka-practice-" + UUID.randomUUID()));

        putIfPresent(props, "security.protocol", "KAFKA_SECURITY_PROTOCOL");
        putIfPresent(props, "sasl.mechanism", "KAFKA_SASL_MECHANISM");
        putIfPresent(props, "sasl.jaas.config", "KAFKA_SASL_JAAS_CONFIG");
        putIfPresent(props, "ssl.truststore.location", "KAFKA_SSL_TRUSTSTORE_LOCATION");
        putIfPresent(props, "ssl.truststore.password", "KAFKA_SSL_TRUSTSTORE_PASSWORD");
        putIfPresent(props, "ssl.keystore.location", "KAFKA_SSL_KEYSTORE_LOCATION");
        putIfPresent(props, "ssl.keystore.password", "KAFKA_SSL_KEYSTORE_PASSWORD");
        putIfPresent(props, "ssl.key.password", "KAFKA_SSL_KEY_PASSWORD");

        return props;
    }

    public static String env(String name, String defaultValue) {
        var value = System.getenv(name);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return DOTENV.getOrDefault(name, defaultValue);
    }

    private static String requiredEnv(String name) {
        var value = env(name, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required Kafka configuration: " + name);
        }
        return value;
    }

    private static void putIfPresent(Properties props, String propertyName, String envName) {
        var value = env(envName, null);
        if (value != null && !value.isBlank()) {
            props.put(propertyName, value);
        }
    }

    private static Map<String, String> loadDotenv() {
        if (!Files.exists(ENV_FILE)) {
            System.err.println("Warning: .env file not found. Using process environment and defaults.");
            return Map.of();
        }

        var values = new HashMap<String, String>();
        try {
            for (var line : Files.readAllLines(ENV_FILE)) {
                parseLine(line, values);
            }
        } catch (IOException error) {
            System.err.println("Warning: Could not read .env file: " + error.getMessage());
        }
        return Map.copyOf(values);
    }

    private static void parseLine(String line, Map<String, String> values) {
        var trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }

        if (trimmed.startsWith("export ")) {
            trimmed = trimmed.substring("export ".length()).trim();
        }

        var separator = trimmed.indexOf('=');
        if (separator <= 0) {
            System.err.println("Warning: Ignoring invalid .env line: " + line);
            return;
        }

        var name = trimmed.substring(0, separator).trim();
        var value = trimmed.substring(separator + 1).trim();
        values.put(name, unquote(value));
    }

    private static String unquote(String value) {
        if (value.length() >= 2) {
            var first = value.charAt(0);
            var last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
