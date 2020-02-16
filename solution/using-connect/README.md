```hsqldb
$ cd /training-developer-src/labs
$ docker-compose up -d
```

```bash
$ docker-compose exec postgres psql -U postgres
$ postgres=# CREATE TABLE operators(id int not null primary key,name varchar(50) not null);
```

```bash
curl -s -X POST -H "Content-Type: application/json" \
--data '{
    "name": "Operators-Connector",
    "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "connection.url": "jdbc:postgresql://postgres:5432/postgres",
    "connection.user": "postgres",
    "table.whitelist": "operators",
    "mode":"incrementing",
    "incrementing.column.name": "id",
    "table.types": "TABLE",
    "topic.prefix": "pg-",
    "numeric.mapping": "best_fit",
    "transforms": "createKey,extractInt",
    "transforms.createKey.type":
    "org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields": "id",
    "transforms.extractInt.type":
    "org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractInt.field": "id"
    }
}' http://localhost:8083/connectors | jq
```

```Kafka Avro Console Consumer```
```bash
$ kafka-avro-console-consumer \ 
    --bootstrap-server localhost:19092 \
    --property schema.registry.url=http://localhost:8081 \
    --topic pg-operators \
    --from-beginning \
    --property print.key=true
```


Kafka-Connect
```bash
$ http://localhost:8083/connectors
[
"Operators-Connector"
]
```

Schema Registry:
```bash
$ http://localhost:8081/subjects
[
"pg-operators-value",
"pg-operators-key",
]
```
