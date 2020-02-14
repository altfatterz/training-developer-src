
Consumer

```bash 
$ kafka-avro-console-consumer --bootstrap-server localhost:19092 --topic vehicle-positions-avro --from-beginning --property schema.registry.url=http://localhost:8081
```


```bash
$ curl http://localhost:8081/subjects

[
    "vehicle-positions-avro-key",
    "vehicle-positions-avro-value",
]
```

```bash
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @sample-schema-v1.json localhost:8081/subjects/sample/versions
$ curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data @sample-schema-v2.json localhost:8081/subjects/sample/versions
```

First version

```bash
$ curl -X GET -H "Content-Type: application/vnd.schemaregistry.v1+json" localhost:8081/subjects/sample/versions/1 | jq '.schema | fromjson'
```

Second version
```bash
$ $ curl -X GET -H "Content-Type: application/vnd.schemaregistry.v1+json" localhost:8081/subjects/sample/versions/2 | jq '.schema | fromjson'
```

Delete a version

```bash
$ curl -X DELETE -H "Content-Type: application/vnd.schemaregistry.v1+json" localhost:8081/subjects/sample/versions/2
```

