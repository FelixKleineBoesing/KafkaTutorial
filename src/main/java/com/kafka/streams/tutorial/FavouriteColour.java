package com.kafka.streams.tutorial;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

public class FavouriteColour {

    public static void main(String[] args) {
        String[] allowedColors =  new String[]{"green", "red", "blue"};

        Properties config = new Properties();
        config.put(StreamsConfig.APPLICATION_ID_CONFIG, "favourite-color-app");
        config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> favouriteColor = builder.stream("favourite-color-input");
        favouriteColor.filter((key, value) -> value.contains(",")).
                selectKey((k, v) -> v.split(",")[0].toLowerCase(Locale.ROOT)).
                mapValues(v -> v.split(",")[1].toLowerCase(Locale.ROOT)).
                filter((k, v) -> Arrays.asList(allowedColors).contains(v)).
                to("favourite-color-cleaned", Produced.with(Serdes.String(), Serdes.String()));

        KTable<String, String> favouriteColorCleaned = builder.table("favourite-color-cleaned");

        KTable<String, Long>favoriteColorsCount = favouriteColorCleaned.
                groupBy((k, v) -> new KeyValue<>(v, v)).
                count();

        favoriteColorsCount.toStream().to("favourite-color-output", Produced.with(Serdes.String(), Serdes.Long()));;
        KafkaStreams streams = new KafkaStreams(builder.build(), config);
        streams.cleanUp();
        streams.start();
        System.out.println(streams.toString());
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }

}
