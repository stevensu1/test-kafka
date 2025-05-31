package com.example.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MessageProducer {
    public static void main(String[] args) {
        // 配置属性
        Properties props = new Properties();
        
        // 设置bootstrap servers
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        
        // 设置序列化器
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        // SSL配置
        props.put("security.protocol", "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "D:/kafka_2.13-4.0.0/config/ssl/kafka.server.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "D:/kafka_2.13-4.0.0/config/ssl/kafka.server.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2,TLSv1.3");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");
        
        // 创建生产者
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            // 创建消息
            String topic = "demo-topic";
            String key = "test-key";
            String value = "Hello, Kafka! This is a test message.";
            
            // 发送消息
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    System.out.println("消息发送成功！");
                    System.out.println("Topic: " + metadata.topic());
                    System.out.println("Partition: " + metadata.partition());
                    System.out.println("Offset: " + metadata.offset());
                } else {
                    System.err.println("消息发送失败: " + exception.getMessage());
                }
            }).get(); // 等待发送完成
            
            System.out.println("消息已发送到Kafka");
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("发送消息时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 