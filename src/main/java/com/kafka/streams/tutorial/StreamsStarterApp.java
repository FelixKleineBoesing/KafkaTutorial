package com.kafka.streams.tutorial;
import jdk.internal.org.objectweb.asm.tree.analysis.Value;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueMapper;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

public class StreamsStarterApp {

    public static final String TOPIC_INPUT = "word-count-input";
    public static final String TOPIC_OUTPUT = "word-count-input";

    public static void main(String[] args) {

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-starter-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        Topology topology = createTopology();

        KafkaStreams streams = new KafkaStreams(topology, config);
        streams.start();
        System.out.println(streams.toString());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    public static Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> wordCountInput = builder.stream(TOPIC_INPUT);
        KTable<String, Long> wordCounts = wordCountInput.mapValues(textLine -> textLine.toLowerCase(Locale.ROOT)).
                flatMapValues(line -> Arrays.asList(line.split(" "))).
                selectKey((k, v) ->  {
                    System.out.println(v);
                    return v;
                }).
                groupByKey().
                count();
        wordCounts.toStream().to(TOPIC_OUTPUT, Produced.with(Serdes.String(), Serdes.Long()));
        return builder.build();
    }

}
