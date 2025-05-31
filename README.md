# Kafka KRaft + SSL + SASL/PLAIN 部署文档
本文档介绍如何在 Windows 环境下部署 Kafka 4.x，使用 KRaft 模式、SSL 加密和 SASL/PLAIN 认证。

1. 环境准备
JDK 17 或更高版本
Kafka 4.x 版本（本文档基于 kafka_2.13-4.0.0）
2. 目录结构
D:\kafka_2.13-4.0.0\
├── bin\windows\
├── config\
│   ├── server.properties
│   ├── ssl.properties
│   ├── jaas.conf
│   └── log4j2.properties
├── logs\
└── config\ssl\
    ├── kafka.server.keystore.jks
    └── kafka.server.truststore.jks
    
3. 配置文件说明
3.1 server.properties
# 节点角色配置
process.roles=broker,controller
node.id=1

# 监听器配置
listeners=PLAINTEXT://:9092,CONTROLLER://:9093,SSL://:9094,SASL_SSL://:9095
advertised.listeners=SSL://localhost:9094,SASL_SSL://localhost:9095
controller.listener.names=CONTROLLER
inter.broker.listener.name=SSL
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_SSL:SASL_SSL

# 控制器配置
controller.quorum.voters=1@localhost:9093

# 日志配置
log.dirs=D:/kafka_2.13-4.0.0/logs

# 安全配置
sasl.enabled.mechanisms=PLAIN
sasl.mechanism.inter.broker.protocol=PLAIN
authorizer.class.name=org.apache.kafka.metadata.authorizer.StandardAuthorizer
allow.everyone.if.no.acl.found=true

# SSL 配置
ssl.keystore.location=config/ssl/kafka.server.keystore.jks
ssl.keystore.password=kafka123
ssl.key.password=kafka123
ssl.truststore.location=config/ssl/kafka.server.truststore.jks
ssl.truststore.password=kafka123
ssl.client.auth=required
ssl.enabled.protocols=TLSv1.2,TLSv1.3
ssl.endpoint.identification.algorithm=HTTPS
ssl.secure.random.implementation=SHA1PRNG
    
3.2 ssl.properties
# SSL 配置
ssl.keystore.location=config/ssl/kafka.server.keystore.jks
ssl.keystore.password=kafka123
ssl.key.password=kafka123
ssl.truststore.location=config/ssl/kafka.server.truststore.jks
ssl.truststore.password=kafka123
ssl.client.auth=required
ssl.enabled.protocols=TLSv1.2,TLSv1.3
ssl.endpoint.identification.algorithm=HTTPS
ssl.secure.random.implementation=SHA1PRNG
    
3.3 jaas.conf
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="admin"
    password="admin-secret"
    user_admin="admin-secret"
    user_alice="alice-secret";
};
    
3.4 log4j2.properties
status = INFO
name = KafkaConfig

# 控制台输出
appender.console.type = Console
appender.console.name = console
appender.console.layout.type = PatternLayout
appender.console.layout.pattern = %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# 文件输出
appender.kafka.type = RollingFile
appender.kafka.name = kafka
appender.kafka.fileName = ${sys:kafka.logs.dir}/server.log
appender.kafka.filePattern = ${sys:kafka.logs.dir}/server-%d{yyyy-MM-dd}-%i.log.gz
appender.kafka.layout.type = PatternLayout
appender.kafka.layout.pattern = %d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
appender.kafka.policies.type = Policies
appender.kafka.policies.time.type = TimeBasedTriggeringPolicy
appender.kafka.policies.size.type = SizeBasedTriggeringPolicy
appender.kafka.policies.size.size = 100MB
appender.kafka.strategy.type = DefaultRolloverStrategy
appender.kafka.strategy.max = 10

# 根日志配置
rootLogger.level = INFO
rootLogger.appenderRefs = console, kafka
rootLogger.appenderRef.console.ref = console
rootLogger.appenderRef.kafka.ref = kafka

# Kafka 日志配置
logger.kafka.name = kafka
logger.kafka.level = INFO
logger.kafka.additivity = false
logger.kafka.appenderRefs = console, kafka
logger.kafka.appenderRef.console.ref = console
logger.kafka.appenderRef.kafka.ref = kafka
    
4. 部署步骤
4.1 初始步骤
1. 下载 Kafka 4.x 版本（本文档基于 kafka_2.13-4.0.0）

2. 解压到 D:\kafka_2.13-4.0.0

3. 创建必要的目录：

mkdir D:\kafka_2.13-4.0.0\logs
mkdir D:\kafka_2.13-4.0.0\config\ssl
    
4. 创建配置文件（server.properties、ssl.properties、jaas.conf、log4j2.properties）

4.2 生成 SSL 证书
运行以下命令生成 SSL 证书：

# 1. 生成 CA 私钥和证书
openssl req -new -x509 -keyout ca-key -out ca-cert -days 365 -nodes -subj "/CN=kafka-ca"

# 2. 生成服务器私钥
openssl genrsa -out kafka.server.key 2048

# 3. 生成服务器证书签名请求（CSR）
openssl req -new -key kafka.server.key -out kafka.server.csr -subj "/CN=localhost"

# 4. 使用 CA 证书签名服务器证书
openssl x509 -req -CA ca-cert -CAkey ca-key -in kafka.server.csr -out kafka.server.cert -days 365 -CAcreateserial

# 5. 创建 JKS 格式的密钥库
keytool -import -alias ca -file ca-cert -keystore kafka.server.truststore.jks -storepass kafka123 -noprompt
keytool -import -alias server -file kafka.server.cert -keystore kafka.server.keystore.jks -storepass kafka123 -noprompt

# 6. 将私钥导入密钥库
openssl pkcs12 -export -in kafka.server.cert -inkey kafka.server.key -out kafka.server.p12 -name server -password pass:kafka123
keytool -importkeystore -srckeystore kafka.server.p12 -srcstoretype PKCS12 -destkeystore kafka.server.keystore.jks -deststoretype JKS -srcstorepass kafka123 -deststorepass kafka123

# 7. 移动证书文件到配置目录
move kafka.server.keystore.jks config\ssl\
move kafka.server.truststore.jks config\ssl\
    
注意：

确保已安装 OpenSSL 和 Java keytool
所有证书和密钥文件应妥善保管
密码可以根据需要修改，但要确保与配置文件中的密码一致
4.3 格式化存储目录
在启动 Kafka 服务前，需要格式化存储目录，确保 meta.properties 正确生成：

bin\windows\kafka-storage.bat random-uuid
bin\windows\kafka-storage.bat format -t  -c config\server.properties
    
4.4 启动 Kafka 服务
运行以下命令启动 Kafka 服务：

start-kafka-kraft.bat
    
5. 客户端连接示例
5.1 Java 客户端配置
bootstrap.servers=localhost:9095
security.protocol=SASL_SSL
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="admin" password="admin-secret";
ssl.truststore.location=你的truststore路径
ssl.truststore.password=kafka123
    
5.2 命令行工具配置
使用以下命令创建主题：

bin\windows\kafka-topics.bat --create --topic test-topic --bootstrap-server localhost:9095 --command-config config\client.properties
    
6. 常见问题
问题： No `meta.properties` found in logs directory
解决： 运行 kafka-storage.bat format 命令格式化存储目录。
问题： ClassNotFoundException: kafka.security.authorizer.AclAuthorizer
解决： 将 authorizer.class.name 修改为 org.apache.kafka.metadata.authorizer.StandardAuthorizer。
问题： Log4j 配置错误
解决： 确保使用 Log4j2 配置文件，并在启动脚本中正确设置 KAFKA_LOG4J_OPTS。
7. 参考链接
Kafka 安全文档
Log4j 迁移指南
