package com.kafka.streams.tutorial;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class StreamsStarterAppTest {

    TopologyTestDriver testDriver;
    private TestInputTopic inputTopic;
    private TestOutputTopic outputTopic;

    public static final String TOPIC_INPUT = StreamsStarterApp.TOPIC_INPUT;
    public static final String TOPIC_OUTPUT = StreamsStarterApp.TOPIC_OUTPUT;

    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Long> longSerde = Serdes.Long();
    private final Serde<Bytes> nullSerde = Serdes.Bytes();

    @Before
    public void setUpTopology() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:123");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        Topology topology = StreamsStarterApp.createTopology();
        testDriver = new TopologyTestDriver(topology, props);

        inputTopic = testDriver.createInputTopic(TOPIC_INPUT, nullSerde.serializer(), stringSerde.serializer());
        outputTopic = testDriver.createOutputTopic(TOPIC_OUTPUT, stringSerde.deserializer(), longSerde.deserializer());
    }

    @After
    public void closeTestDriver() {
        testDriver.close();
    }

    @Test
    public void dummyTest() {
        String dummy = "Du" + "mmy";
        assertEquals(dummy, "Dummy");
    }

    @Test
    public void assertCountsAreCorrect() {
        System.out.println("Test");
        inputTopic.pipeInput(null, "Testing Stream Kafka", 1L);
        System.out.println("Test");
        final Object output = outputTopic.readValue();
        System.out.println(output);
    }
}
