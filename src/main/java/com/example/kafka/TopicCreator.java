package com.example.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.SslConfigs;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class TopicCreator {
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
        
        // 创建AdminClient
        try (AdminClient admin = AdminClient.create(props)) {
            // 创建topic
            NewTopic newTopic = new NewTopic("demo-topic", 1, (short) 1);
            CreateTopicsResult result = admin.createTopics(Collections.singleton(newTopic));
            
            // 等待创建完成
            result.all().get();
            System.out.println("Topic创建成功！");
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("创建Topic时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 