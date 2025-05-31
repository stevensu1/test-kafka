package com.example.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class MessageConsumer {
    public static void main(String[] args) {
        // 配置属性
        Properties props = new Properties();
        
        // 设置bootstrap servers
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        
        // 设置序列化器
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        
        // 设置从头开始消费
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        
        // SSL配置
        props.put("security.protocol", "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "D:/kafka_2.13-4.0.0/config/ssl/kafka.server.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "D:/kafka_2.13-4.0.0/config/ssl/kafka.server.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2,TLSv1.3");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");
        
        // 创建消费者
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // 直接指定主题和分区
            String topic = "demo-topic";
            TopicPartition partition = new TopicPartition(topic, 0);
            consumer.assign(Collections.singleton(partition));
            
            // 从头开始消费
            consumer.seekToBeginning(Collections.singleton(partition));
            
            System.out.println("开始消费消息...");
            
            // 持续消费消息
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("收到消息：");
                    System.out.println("Topic: " + record.topic());
                    System.out.println("Partition: " + record.partition());
                    System.out.println("Offset: " + record.offset());
                    System.out.println("Key: " + record.key());
                    System.out.println("Value: " + record.value());
                    System.out.println("------------------------");
                }
            }
        } catch (Exception e) {
            System.err.println("消费消息时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 