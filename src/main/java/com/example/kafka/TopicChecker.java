package com.example.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class TopicChecker {
    public static void main(String[] args) {
        // 配置属性
        Properties props = new Properties();
        
        // 设置bootstrap servers
        props.put("bootstrap.servers", "localhost:9094");
        
        // SSL配置
        props.put("security.protocol", "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "D:/kafka_2.13-4.0.0/config/ssl/kafka.server.truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "D:/kafka_2.13-4.0.0/config/ssl/kafka.server.keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "kafka123");
        props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2,TLSv1.3");
        props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");
        
        try (AdminClient admin = AdminClient.create(props)) {
            // 列出所有主题
            ListTopicsResult listTopics = admin.listTopics();
            Set<String> topics = listTopics.names().get();
            System.out.println("所有主题列表：");
            topics.forEach(System.out::println);
            
            // 检查特定主题
            String topicName = "demo-topic";
            if (topics.contains(topicName)) {
                DescribeTopicsResult describeTopics = admin.describeTopics(Collections.singleton(topicName));
                Map<String, TopicDescription> topicDescriptions = describeTopics.allTopicNames().get();
                System.out.println("\n主题 " + topicName + " 的详细信息：");
                topicDescriptions.forEach((topic, description) -> {
                    System.out.println("主题名称: " + topic);
                    System.out.println("分区数: " + description.partitions().size());
                    description.partitions().forEach(partition -> {
                        System.out.println("  分区 " + partition.partition() + ":");
                        System.out.println("    领导者: " + partition.leader().id());
                        System.out.println("    副本: " + partition.replicas());
                        System.out.println("    ISR: " + partition.isr());
                    });
                });
                
                // 检查消息
                checkTopicMessages(props, topicName);
            } else {
                System.out.println("\n主题 " + topicName + " 不存在！");
            }
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("检查主题时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkTopicMessages(Properties props, String topicName) {
        // 添加消费者配置
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("group.id", "checker-group");
        props.put("auto.offset.reset", "earliest");
        
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            // 获取主题的所有分区
            TopicPartition partition = new TopicPartition(topicName, 0);
            consumer.assign(Collections.singleton(partition));
            
            // 获取分区的开始和结束偏移量
            long beginningOffset = consumer.beginningOffsets(Collections.singleton(partition)).get(partition);
            long endOffset = consumer.endOffsets(Collections.singleton(partition)).get(partition);
            
            System.out.println("\n主题 " + topicName + " 的消息信息：");
            System.out.println("开始偏移量: " + beginningOffset);
            System.out.println("结束偏移量: " + endOffset);
            System.out.println("消息数量: " + (endOffset - beginningOffset));
            
            if (endOffset > beginningOffset) {
                System.out.println("\n尝试读取消息...");
                consumer.seekToBeginning(Collections.singleton(partition));
                var records = consumer.poll(Duration.ofSeconds(5));
                records.forEach(record -> {
                    System.out.println("消息内容：");
                    System.out.println("  Key: " + record.key());
                    System.out.println("  Value: " + record.value());
                    System.out.println("  Offset: " + record.offset());
                    System.out.println("  Timestamp: " + record.timestamp());
                });
            }
        }
    }
} 