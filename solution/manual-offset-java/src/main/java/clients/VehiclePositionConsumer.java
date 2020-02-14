package clients;

import java.time.Duration;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class VehiclePositionConsumer {
    final static String OFFSET_FILE_PREFIX = "solution/manual-offset-java/offsets/offset_";

    public static void main(String[] args) throws IOException {
        System.out.println("*** Starting VP Consumer ***");
        
        Properties settings = new Properties();
        settings.put(ConsumerConfig.GROUP_ID_CONFIG, "manual-offset-consumer");
        settings.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        settings.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        settings.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        settings.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(settings);
        ConsumerRebalanceListener listener = createListener(consumer);
        try {
            consumer.subscribe(Arrays.asList("vehicle-positions"), listener);

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                Set<Integer> partitions = new HashSet<>();
                for (ConsumerRecord<String, String> record : records) {
                    partitions.add(record.partition());

                    Files.write(Paths.get(OFFSET_FILE_PREFIX + record.partition()),
                            Long.valueOf(record.offset() + 1).toString().getBytes());
                }
                System.out.printf("Processed in 100 ms: %d records from partitions: %s\n", records.count(), partitions);
            }
        }
        finally{
            System.out.println("*** Ending VP Consumer ***");
            consumer.close();
        }
    }

    private static ConsumerRebalanceListener createListener(KafkaConsumer<String, String> consumer){
        return new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // nothing to do...
            }
            
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                for (TopicPartition partition : partitions) {
                    try{
                        if (Files.exists(Paths.get(OFFSET_FILE_PREFIX + partition.partition()))) {
                            long offset = Long.parseLong(Files.readAllLines(
                                    Paths.get(OFFSET_FILE_PREFIX + partition.partition()), Charset.defaultCharset()).get(0));
                            consumer.seek(partition, offset);
                        }
                    } catch(IOException e) {
                        System.out.printf("ERR: Could not read offset from file.\n");
                    }
                }
            }
        };
    }
}