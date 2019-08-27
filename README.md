# Druid metrics exporter
- Enable druid metric emitter
- Format druid metrics following the [Prometheus](https://prometheus.io/) standards
- Expose Promethus metrics

# Running
- cd druid-metrics-exporter
- mvn  clean install -DskipTests
- cd target/
- java -jar druid-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar

By default metrics are exposed on TCP port 9001.

# Build docker image
- cp druid-metrics-1.0-SNAPSHOT-jar-with-dependencies.jar ../src/main/resources/docker/druid-metrics.jar
- cd ../src/main/resources/docker
- docker build -t ecr.vip.ebayc3.com/appmon/druid-metrics:201908262123 .

# API

### Get metrics

`GET http://locahost:9001/api/metrics`

### Post metrics

`POST http://locahost:9001`

Request Body

```
{
    "dataSource":"dataSource",
    "feed":"metrics",
    "host":"ip",
    "id":"abd9b821-04f3-47fe-9d3b-21eb06fcd1d6",
    "metric":"metric name",
    "service":"druid/broker",
    "timestamp":"2019-08-21T01:41:17.936Z",
    "type":"groupBy",
    "value":49,
    "version":"0.12.2"
}
```

# Supported Druid version
Currently we just support Druid 0.12.2 version. The exposed metrics are in [metric.html](https://druid.io/docs/0.12.2/operations/metrics.html).
Later we'll support higher version metrics.

# Available metrics and labels
`service`, `host` and `version` are basic labels of all metrics.
# Query Metrics
## Broker
- `query/time` [metric, dataSource, type, remoteAddress]
- `query/bytes` [dataSource, type, remoteAddress]
- `query/node/time` [server]
- `query/node/bytes` [server]
- `query/node/ttfb` [server]
- `query/intervalChunk/time` [server]
- `query/success/count`[metric]
- `query/failed/count`[metric]
- `query/interrupted/count`[metric]

## Historical
- `query/time` [metric, dataSource, type, remoteAddress]
- `query/segment/time`
- `query/wait/time`[metric]
- `segment/scan/pending`[metric]
- `query/success/count`[metric]
- `query/failed/count`[metric]
- `query/interrupted/count`[metric]

## Jetty
- `jetty/numOpenConnections`

## Cache
- `query/cache/delta/numEntries`
- `query/cache/delta/sizeBytes`
- `query/cache/delta/hits`
- `query/cache/delta/misses`
- `query/cache/delta/evictions`
- `query/cache/delta/hitRate`
- `query/cache/delta/averageByte`
- `query/cache/delta/timeouts`
- `query/cache/delta/errors`
- `query/cache/total/numEntries`
- `query/cache/total/sizeBytes`
- `query/cache/total/hits`
- `query/cache/total/misses`
- `query/cache/total/evictions`
- `query/cache/total/hitRate`
- `query/cache/total/averageByte`
- `query/cache/total/timeouts`
- `query/cache/total/errors`

## Ingestion Metrics
- `ingest/events/thrownAway`[dataSource]
- `ingest/events/unparseable`[dataSource]
- `ingest/events/processed`[dataSource]
- `ingest/rows/output`[dataSource]
- `ingest/persists/count`[dataSource]
- `ingest/persists/time`[dataSource]
- `ingest/persists/cpu`[dataSource]
- `ingest/persists/backPressure`[dataSource]
- `ingest/persists/failed`[dataSource]
- `ingest/handoff/failed`[dataSource]
- `ingest/merge/time`[dataSource]
- `ingest/merge/cpu`[dataSource]
- `ingest/handoff/count`[dataSource]
- `ingest/sink/count`[dataSource]
- `ingest/events/messageGap`[dataSource]
- `ingest/kafka/lag`[dataSource]
## Indexing Service
- `task/run/time`[dataSource, taskType, taskStatus]
- `segment/added/bytes`[dataSource, taskType]
- `segment/moved/bytes`[dataSource, taskType]
- `segment/nuked/bytes`[dataSource, taskType]

## Coordinator
- `segment/assigned/count`[tier]
- `segment/moved/count`[tier]
- `segment/dropped/count`[tier]
- `segment/deleted/count`[tier]
- `segment/unneeded/count`[tier]
- `segment/cost/raw`[tier]
- `segment/cost/normalization`[tier]
- `segment/cost/normalized`[tier]
- `segment/loadQueue/size`[tier]
- `segment/loadQueue/failed`[tier]
- `segment/loadQueue/count`[tier]
- `segment/dropQueue/count`[tier]
- `segment/size`[tier]
- `segment/count`[tier]
- `segment/overShadowed/count`[tier]
- `segment/unavailable/count`[tier]
- `segment/underReplicated/count`[tier]

# General Health
## Historical
- `segment/max`
- `segment/used`[dataSource, tier, priority]
- `segment/usedPercent`[dataSource, tier, priority]
- `segment/count`[dataSource, tier, priority]
- `segment/pendingDelete`

## JVM
- `jvm/pool/committed`[poolKind, poolName]
- `jvm/pool/init`[poolKind, poolName]
- `jvm/pool/max`[poolKind, poolName]
- `jvm/pool/used`[poolKind, poolName]
- `jvm/bufferpool/count`[bufferPoolName]
- `jvm/bufferpool/used`[bufferPoolName]
- `jvm/bufferpool/capacity`[bufferPoolName]
- `jvm/mem/init`[memKind]
- `jvm/bufferpool/max`[memKind]
- `jvm/bufferpool/used`[memKind]
- `jvm/mem/committed`[memKind]
- `jvm/gc/count`[gcName]
- `jvm/gc/cpu`[gcName]

Note:Metric `jvm/gc/cpu` is correct name not `jvm/gc/time` which is wrong in Druid document.

