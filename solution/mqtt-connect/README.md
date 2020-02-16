Creates a custom connect image installing the `mqtt` connector (see `connect/Dockerfile`)

```bash
$ cd ~/confluent-dev/labs/mqtt-connect
$ docker-compose build
$ docker-compose up -d
```

Execute the next commands within the `tools` container 

```bash
$ docker-compose exec tools /bin/bash
```

Install `Mosquitto` a `MQTT` client

```bash
su && \
apt-get update && \
apt-get install -y software-properties-common && \
apt-add-repository ppa:mosquitto-dev/mosquitto-ppa && \
apt-get install -y mosquitto-clients
```

Try it out that we can get data:

```bash
$ mosquitto_sub -h mqtt.hsl.fi -p 1883 -d -t "/hfp/v2/journey/ongoing/vp/#"
```

Create a topic

```bash
kafka-topics \
--create \
--bootstrap-server kafka:9092 \
--partitions 6 \
--replication-factor 1 \
--topic vehicle-positions
```

Configure the MQTT connector

```bash
curl -s -X POST -H 'Content-Type: application/json' -d '{
"name" : "mqtt-source",
"config" : {
"connector.class" : "io.confluent.connect.mqtt.MqttSourceConnector",
"tasks.max" : "1",
"mqtt.server.uri" : "tcp://mqtt.hsl.fi:1883",
"mqtt.topics" : "/hfp/v2/journey/ongoing/vp/#",
"kafka.topic" : "vehicle-positions",
"confluent.topic.bootstrap.servers": "kafka:9092",
"confluent.topic.replication.factor": "1",
"confluent.license":"",
"key.converter": "org.apache.kafka.connect.storage.StringConverter",
"value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter"
}
}' http://connect:8083/connectors | jq .
```

Verify that it was created

```bash
$ curl -s "http://connect:8083/connectors"
$ curl -s "http://connect:8083/connectors/mqtt-source/status" | jq .
```

```bash
$ kafka-console-consumer --bootstrap-server kafka:9092 \
--topic vehicle-positions \
--from-beginning \
--max-messages 5
```