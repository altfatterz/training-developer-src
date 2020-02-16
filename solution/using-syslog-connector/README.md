
Creates a custom connect image installing the `syslog` connector (see `connect/Dockerfile`)

```bash
$ cd ~/confluent-dev/labs/using-syslog-connector
$ docker-compose build
$ docker-compose up -d
```

Execute the next commands within the `tools` container 

```bash
$ docker-compose exec tools /bin/bash
```

Create the `syslog` topic

```bash
kafka-topics \
--create \
--bootstrap-server kafka:9092 \
--partitions 6 \
--replication-factor 1 \
--topic syslog
```

Register the Syslog connector

```bash
curl -s -X POST \
-H "Content-Type: application/json" \
--data '{
"name": "Syslog-Connector",
"config": {
"connector.class": "io.confluent.connect.syslog.SyslogSourceConnector",
"tasks.max": "1",
"syslog.port": "5454",
"syslog.listener": "TCP",
"confluent.topic.bootstrap.servers": "kafka:9092",
"confluent.topic.replication.factor": "1",
"confluent.license":""
}
}' http://connect:8083/connectors | jq
```

Check that the connector was configured:

```bash
$ curl http://connect:8083/connectors
["Sylog-connectors"]
```

Check the status of the connector:

```bash
$ curl -s "http://localhost:8083/connectors/Syslog-Connector/status" | jq .
{
  "name": "Syslog-Connector",
  "connector": {
    "state": "RUNNING",
    "worker_id": "connect:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "connect:8083"
    }
  ],
  "type": "source"
}
```

Simulate a syslog message

```bash
$ echo "<34>1 2003-10-11T22:14:15.003Z host1.acme.com su - ID47 - Engine started" | nc -w 1 connect 5454
```

Verify that the message was received in the `syslog` topic

```
$ kafka-avro-console-consumer \
  --bootstrap-server kafka:9092 \
  --property schema.registry.url=http://schema-registry:8081 \
  --topic syslog \
  --from-beginning | jq '.'
```