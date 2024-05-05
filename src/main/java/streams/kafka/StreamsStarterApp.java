package streams.kafka;

import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;

public class StreamsStarterApp {

  public static void main(String[] args) {

    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-application");
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    StreamsBuilder builder = new StreamsBuilder();

    final var wordCountInput = builder.stream("word-count-input");

    final var wordCount = wordCountInput
        .mapValues(values -> ((String) values).toLowerCase())
        .flatMapValues(value -> List.of(value.split(" ")))
        .selectKey((key, value) -> value)
        .groupByKey()
        .count(Named.as("Counts"));

    wordCount.toStream().to("word-count-output", Produced.with(Serdes.String(), Serdes.Long()));

    final var streams = new KafkaStreams(builder.build(), config);
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }
}
