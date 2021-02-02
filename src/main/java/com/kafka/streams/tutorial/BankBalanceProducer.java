package com.kafka.streams.tutorial;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.Random;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class BankBalanceProducer {

    private final static String TOPIC = "bank-balance-input";
    private final static String BOOTSTRAP_SERVERS = "localhost:9092";
    private final static String CLIENT_ID_CONFIG = "BankBalanceProducer";
    private final static String KEY_SERIALIZER_CLASS_CONFIG = StringSerializer.class.getName();
    private final static String VALUE_SERIALIZER_CLASS_CONFIG = StringSerializer.class.getName();
    private final static String[] CUSTOMER_NAMES = new String[]{"Felix", "Kim", "Fardad", "Jason", "Verena", "Matis"};

    public static void main(String[] args) {
        runProducer();
    }

    private static Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER_CLASS_CONFIG);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER_CLASS_CONFIG);
        return new KafkaProducer<>(props);

    }

    static void runProducer() {
        final Producer<String, String> producer = createProducer();

        int i = 0;
        while (true) {
            System.out.println("Batch No.: " + i);
            try {
                Random rand = new Random();
                final int amount = rand.nextInt(1500);
                final String name = CUSTOMER_NAMES[rand.nextInt(CUSTOMER_NAMES.length)];
                String time = Instant.now().toString();
                ObjectNode transaction = JsonNodeFactory.instance.objectNode();
                transaction.
                        put("name", name).
                        put("amount", amount).
                        put("time", time);


                final ProducerRecord<String, String> record =
                        new ProducerRecord<String, String>(TOPIC, name, transaction.toString());
                i += 1;
                Thread.sleep(10);

                producer.send(record);

                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }



            } catch (InterruptedException e) {
                producer.flush();
                producer.close();
                break;
            }
        }

    }
}
