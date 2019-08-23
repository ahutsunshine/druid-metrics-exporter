package com.ebay.druid;

import com.google.common.collect.Maps;
import io.prometheus.client.*;
import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import static com.ebay.druid.MetricsReporter.Metric.*;
import static com.ebay.druid.util.TimeUtil.getTime;

public class MetricsReporter {

    private static final Logger LOG = LoggerFactory.getLogger(MetricsReporter.class);

    public static final double ONE_SECOND = 1_000_000_000.0;

    private static final MetricsReporter INSTANCE = new MetricsReporter();

    private volatile boolean initialized;

    private CollectorRegistry registry = CollectorRegistry.defaultRegistry;
    private static final Map<Metric, SimpleCollector> metricsInCache = Maps.newConcurrentMap();
    public static final Map<String, Metric> metricMap = Maps.newConcurrentMap();

    public static MetricsReporter getInstance() {
        if (metricMap.size() == 0) {
            registerMetrics();
        }
        return INSTANCE;
    }

    public static void registerMetrics() {

        //Common metrics
        metricMap.put("query/time", queryTime);
        metricMap.put("query/success/count", querySuccessCount);
        metricMap.put("query/failed/count", queryFailedCount);
        metricMap.put("query/interrupted/count", queryInterruptedCount);
        metricMap.put("query/wait/time", queryWaitTime);
        metricMap.put("segment/scan/pending", segmentScanPending);

        //Broker
        metricMap.put("query/bytes", brokerQueryBytes);
        metricMap.put("query/node/time", brokerQueryNodeTime);
        metricMap.put("query/node/bytes", brokerQueryNodeBytes);
        metricMap.put("query/node/ttfb", brokerQueryNodeTtfb);
        metricMap.put("query/intervalChunk/time", brokerQueryIntervalChunkTime);

        //Historical
        metricMap.put("query/segment/time", historicalQuerySegmentTime);
        metricMap.put("query/segmentAndCache/time", historicalQuerySegmentAndCacheTime);
        metricMap.put("query/cpu/time", historicalQueryCpuTime);

        //Jetty
        metricMap.put("jetty/numOpenConnections", jettyNumOpenConnections);

        //Cache
        metricMap.put("query/cache/delta/numEntries", queryCacheDeltaNumEntries);
        metricMap.put("query/cache/total/numEntries", queryCacheTotalNumEntries);
        metricMap.put("query/cache/delta/sizeBytes", queryCacheDeltaSizeBytes);
        metricMap.put("query/cache/total/sizeBytes", queryCacheTotalSizeBytes);
        metricMap.put("query/cache/delta/hits", queryCacheDeltaHits);
        metricMap.put("query/cache/total/hits", queryCacheTotalHits);
        metricMap.put("query/cache/delta/misses", queryCacheDeltaMisses);
        metricMap.put("query/cache/total/misses", queryCacheTotalMisses);
        metricMap.put("query/cache/delta/evictions", queryCacheDeltaEvictions);
        metricMap.put("query/cache/total/evictions", queryCacheTotalEvictions);
        metricMap.put("query/cache/delta/hitRate", queryCacheDeltaHitRate);
        metricMap.put("query/cache/total/hitRate", queryCacheTotalHitRate);
        metricMap.put("query/cache/delta/averageBytes", queryCacheDeltaAverageBytes);
        metricMap.put("query/cache/total/averageBytes", queryCacheTotalAverageBytes);
        metricMap.put("query/cache/delta/timeouts", queryCacheDeltaTimeouts);
        metricMap.put("query/cache/total/timeouts", queryCacheTotalTimeouts);
        metricMap.put("query/cache/delta/errors", queryCacheDeltaErrors);
        metricMap.put("query/cache/total/errors", queryCacheTotalErrors);

        //Ingestion metrics
        metricMap.put("ingest/events/thrownAway", ingestEventsThrownAway);
        metricMap.put("ingest/events/unparseable", ingestEventsUnparseable);
        metricMap.put("ingest/events/processed", ingestEventsProcessed);
        metricMap.put("ingest/rows/output", ingestRowsOutput);
        metricMap.put("ingest/persists/count", ingestPersistsCount);
        metricMap.put("ingest/persists/time", ingestPersistsTime);
        metricMap.put("ingest/persists/cpu", ingestPersistsCpu);
        metricMap.put("ingest/persists/backPressure", ingestPersistsBackPressure);
        metricMap.put("ingest/persists/failed", ingestPersistsFailed);
        metricMap.put("ingest/handoff/failed", ingestHandoffFailed);
        metricMap.put("ingest/merge/time", ingestMergeTime);
        metricMap.put("ingest/merge/cpu", ingestMergeCpu);
        metricMap.put("ingest/handoff/count", ingestHandoffCount);
        metricMap.put("ingest/sink/count", ingestSinkCount);
        metricMap.put("ingest/events/messageGap", ingestEventsMessageGap);
        metricMap.put("ingest/kafka/lag", ingestKafkaLag);

        //Indexing service
        metricMap.put("task/run/time", taskRunTime);
        metricMap.put("segment/added/bytes", segmentAddedBytes);
        metricMap.put("segment/moved/bytes", segmentMovedBytes);
        metricMap.put("segment/nuked/bytes", segmentNukedBytes);

        //Coordination
        metricMap.put("segment/assigned/count", coordinatorSegmentAssignedCount);
        metricMap.put("segment/moved/count", coordinatorSegmentMovedCount);
        metricMap.put("segment/dropped/count", coordinatorSegmentDroppedCount);
        metricMap.put("segment/deleted/count", coordinatorSegmentDeletedCount);
        metricMap.put("segment/unneeded/count", coordinatorSegmentUnneededCount);
        metricMap.put("segment/cost/raw", coordinatorSegmentCostRaw);
        metricMap.put("segment/cost/normalization", coordinatorSegmentCostNormalization);
        metricMap.put("segment/cost/normalized", coordinatorSegmentCostNormalized);
        metricMap.put("segment/loadQueue/size", coordinatorSegmentLoadQueueSize);
        metricMap.put("segment/loadQueue/failed", coordinatorSegmentLoadQueueFailed);
        metricMap.put("segment/loadQueue/count", coordinatorSegmentLoadQueueCount);
        metricMap.put("segment/dropQueue/count", coordinatorSegmentDropQueueCount);
        metricMap.put("segment/size", coordinatorSegmentSize);
        metricMap.put("coordinator/segment/count", coordinatorSegmentCount);//the same with historical, so add coordination prefix
        metricMap.put("segment/overShadowed/count", coordinatorSegmentOverShadowedCount);
        metricMap.put("segment/unavailable/count", coordinatorSegmentUnavailableCount);
        metricMap.put("segment/underReplicated/count", coordinatorSegmentUnderReplicatedCount);

        //General Health
        //Historical
        metricMap.put("segment/max", historicalSegmentMax);
        metricMap.put("segment/used", historicalSegmentUsed);
        metricMap.put("segment/usedPercent", historicalSegmentUsedPercent);
        metricMap.put("historical/segment/count", historicalSegmentCount);//the same with coordination, so add historical prefix
        metricMap.put("segment/pendingDelete", historicalPendingDelete);

        //JVM
        metricMap.put("jvm/pool/committed", jvmPoolCommitted);
        metricMap.put("jvm/pool/init", jvmPoolInit);
        metricMap.put("jvm/pool/max", jvmPoolMax);
        metricMap.put("jvm/pool/used", jvmPoolUsed);
        metricMap.put("jvm/bufferpool/count", jvmBufferpoolCount);
        metricMap.put("jvm/bufferpool/used", jvmBufferpoolUsed);
        metricMap.put("jvm/bufferpool/capacity", jvmBufferpoolCapacity);
        metricMap.put("jvm/mem/init", jvmMemInit);
        metricMap.put("jvm/mem/max", jvmMemMax);
        metricMap.put("jvm/mem/used", jvmMemUsed);
        metricMap.put("jvm/mem/committed", jvmMemCommitted);
        metricMap.put("jvm/gc/count", jvmGcCount);
        metricMap.put("jvm/gc/time", jvmGcTime);

        //EventReceiverFirehose
        metricMap.put("ingest/events/buffered", ingestEventsBuffered);
        metricMap.put("ingest/bytes/received", ingestBytesReceived);

        //Sys
        metricMap.put("sys/swap/free", sysSwapFree);
        metricMap.put("sys/swap/max", sysSwapMax);
        metricMap.put("sys/swap/pageIn", sysSwapPageIn);
        metricMap.put("sys/swap/pageOut", sysSwapPageOut);
        metricMap.put("sys/disk/write/count", sysDiskWriteCount);
        metricMap.put("sys/disk/read/count", sysDiskReadCount);
        metricMap.put("sys/disk/write/size", sysDiskWriteSize);
        metricMap.put("sys/disk/read/size", sysDiskReadSize);
        metricMap.put("sys/net/write/size", sysNetWriteSize);
        metricMap.put("sys/net/read/size", sysNetReadSize);
        metricMap.put("sys/fs/used", sysFsUsed);
        metricMap.put("sys/fs/max", sysFsMax);
        metricMap.put("sys/mem/used", sysMemUsed);
        metricMap.put("sys/mem/max", sysMemMax);
        metricMap.put("sys/storage/used", sysStorageUsed);
        metricMap.put("sys/cpu", sysCpu);
    }

    public enum Metric {
        //druid alerts
        alertUnexpectedException("alerts_unexpected_exception", "unexpected situations generated by Druid", "service", "host", "version", "severity"),

        //Common metrics
        queryTime("query_time", "Milliseconds taken to complete a query", "metric", "service", "host", "version", "dataSource", "type", "remoteAddress"),
        //This metric is only available if the QueryCountStatsMonitor module is included.
        querySuccessCount("query_success_count", "Number of queries successfully processed", "metric", "service", "host", "version"),
        queryFailedCount("query_failed_count", "Number of failed queries", "metric", "service", "host", "version"),
        queryInterruptedCount("query_interrupted_count", "Number of queries interrupted due to cancellation or timeout", "metric", "service", "host", "version"),

        queryWaitTime("query_wait_time", "Milliseconds spent waiting for a segment to be scanned", "metric", "service", "host", "version"),
        segmentScanPending("segment_scan_pending", "Number of segments in queue waiting to be scanned", "metric", "service", "host", "version"),


        //Broker
        brokerQueryBytes("broker_query_bytes", "Number of bytes returned in query response", "service", "host", "version", "dataSource", "type", "remoteAddress"),
        brokerQueryNodeTime("broker_query_node_time", "Milliseconds taken to query individual historical/realtime nodes", "service", "host", "version", "server"),
        brokerQueryNodeBytes("broker_query_node_bytes", "Number of bytes returned from querying individual historical/realtime nodes", "service", "host", "version", "server"),
        brokerQueryNodeTtfb("broker_query_node_ttfb", "Time to first byte. Milliseconds elapsed until broker starts receiving the response from individual historical/realtime nodes", "service", "host", "version", "server"),
        brokerQueryIntervalChunkTime("broker_query_internal_chunk_time", "Only emitted if interval chunking is enabled. Milliseconds required to query an interval chunk", "service", "host", "version", "server"),

        //Historical
        historicalQuerySegmentTime("historical_query_segment_time", "Milliseconds taken to query individual segment. Includes time to page in the segment from disk", "service", "host", "version"),
        historicalQuerySegmentAndCacheTime("historical_query_segment_and_cache_time", "Milliseconds taken to query individual segment or hit the cache (if it is enabled on the historical node)", "service", "host", "version"),
        historicalQueryCpuTime("historical_query_cpu_time", "Microseconds of CPU time taken to complete a query", "service", "host", "version", "dataSource", "type"),

        //Jetty
        jettyNumOpenConnections("jetty_num_open_connections", "Number of open jetty connections", "service", "host", "version"),
        jettyOpenConnectionsCount("jetty_open_connections_count", "Number of open jetty connections", "service", "host", "version"),
        //Cache
        //query/cache/delta/*	Cache metrics since the last emission.
        //query/cache/total/*	Total cache metrics.
        queryCacheDeltaNumEntries("query_cache_delta_num_entries", "Number of cache entries", "service", "host", "version"),
        queryCacheTotalNumEntries("query_cache_total_num_entries", "Number of cache entries", "service", "host", "version"),
        queryCacheDeltaSizeBytes("query_cache_delta_size_bytes", "Size in bytes of cache entries", "service", "host", "version"),
        queryCacheTotalSizeBytes("query_cache_total_size_bytes", "Size in bytes of cache entries", "service", "host", "version"),
        queryCacheDeltaHits("query_cache_delta_hits", "Number of cache hits", "service", "host", "version"),
        queryCacheTotalHits("query_cache_total_hits", "Number of cache hits", "service", "host", "version"),
        queryCacheDeltaMisses("query_cache_delta_misses", "Number of cache misses", "service", "host", "version"),
        queryCacheTotalMisses("query_cache_total_misses", "Number of cache misses", "service", "host", "version"),
        queryCacheDeltaEvictions("query_cache_delta_evictions", "Number of cache evictions", "service", "host", "version"),
        queryCacheTotalEvictions("query_cache_total_evictions", "Number of cache evictions", "service", "host", "version"),
        queryCacheDeltaHitRate("query_cache_delta_hitRate", "Cache hit rate", "service", "host", "version"),
        queryCacheTotalHitRate("query_cache_total_hitRate", "Cache hit rate", "service", "host", "version"),
        queryCacheDeltaAverageBytes("query_cache_delta_average_bytes", "Average cache entry byte size", "service", "host", "version"),
        queryCacheTotalAverageBytes("query_cache_total_average_bytes", "Average cache entry byte size", "service", "host", "version"),
        queryCacheDeltaTimeouts("query_cache_delta_timeouts", "Number of cache timeouts", "service", "host", "version"),
        queryCacheTotalTimeouts("query_cache_total_timeouts", "Number of cache timeouts", "service", "host", "version"),
        queryCacheDeltaErrors("query_cache_delta_errors", "Number of cache errors", "service", "host", "version"),
        queryCacheTotalErrors("query_cache_total_errors", "Number of cache errors", "service", "host", "version"),

        //Ingestion metrics which corresponds to overlord in 0.12.2 version. These metrics are only available if the RealtimeMetricsMonitor is included in the monitors list for the Realtime node.
        // These metrics are deltas for each emission period.
        ingestEventsThrownAway("ingest_events_thrown_away", "Number of events rejected because they are outside the windowPeriod", "service", "host", "version", "dataSource"),
        ingestEventsUnparseable("ingest_events_unparseable", "Number of events rejected because the events are unparseable", "service", "host", "version", "dataSource"),
        ingestEventsProcessed("ingest_events_processed", "Number of events successfully processed per emission period", "service", "host", "version", "dataSource"),
        ingestRowsOutput("ingest_rows_output", "Number of Druid rows persisted", "service", "host", "version", "dataSource"),
        ingestPersistsCount("ingest_persists_count", "Number of times persist occurred", "service", "host", "version", "dataSource"),
        ingestPersistsTime("ingest_persists_time", "Milliseconds spent doing intermediate persist", "service", "host", "version", "dataSource"),
        ingestPersistsCpu("ingest_persists_cpu", "Cpu time in Nanoseconds spent on doing intermediate persist", "service", "host", "version", "dataSource"),
        ingestPersistsBackPressure("ingest_persists_back_pressure", "Milliseconds spent creating persist tasks and blocking waiting for them to finish", "service", "host", "version", "dataSource"),
        ingestPersistsFailed("ingest_persists_failed", "Number of persists that failed", "service", "host", "version", "dataSource"),
        ingestHandoffFailed("ingest_handoff_failed", "Number of handoffs that failed", "service", "host", "version", "dataSource"),
        ingestMergeTime("ingest_merge_time", "Milliseconds spent merging intermediate segments", "service", "host", "version", "dataSource"),
        ingestMergeCpu("ingest_merge_failed", "Cpu time in Nanoseconds spent on merging intermediate segments", "service", "host", "version", "dataSource"),
        ingestHandoffCount("ingest_handoff_count", "Number of handoffs that happened", "service", "host", "version", "dataSource"),
        ingestSinkCount("ingest_sink_count", "Number of sinks not handoffed", "service", "host", "version", "dataSource"),
        ingestEventsMessageGap("ingest_events_message_gap", "Time gap between the data time in event and current system time", "service", "host", "version", "dataSource"),
        ingestKafkaLag("ingest_kafka_lag", "Applicable for Kafka Indexing Service. Total lag between the offsets consumed by the Kafka indexing tasks and latest offsets in Kafka brokers across all partitions. Minimum emission period for this metric is a minute", "service", "host", "version", "dataSource"),

        //Indexing service metrics which corresponds to overlord in 0.12.2 version
        taskRunTime("task_run_time", "Milliseconds taken to run task", "service", "host", "version", "dataSource", "taskType", "taskStatus"),
        taskRunTimeCount("task_run_time_nums", "Number of complete tasks", "service", "host", "version", "dataSource", "taskType", "taskStatus","duration"),
        segmentAddedBytes("segment_added_bytes", "Size in bytes of new segments created", "service", "host", "version", "dataSource", "taskType"),
        segmentMovedBytes("segment_moved_bytes", "Size in bytes of segments moved/archived via the Move Task", "service", "host", "version", "dataSource", "taskType"),
        segmentNukedBytes("segment_nuked_bytes", "Size in bytes of segments deleted via the Kill Task", "service", "host", "version", "dataSource", "taskType"),

        //Coordination: These metrics are for the Druid coordinator and are reset each time the coordinator runs the coordination logic.
        coordinatorSegmentAssignedCount("coordinator_segment_assigned_count", "Number of segments assigned to be loaded in the cluster", "service", "host", "version", "tier"),
        coordinatorSegmentMovedCount("coordinator_segment_moved_count", "Number of segments moved in the cluster", "service", "host", "version", "tier"),
        coordinatorSegmentDroppedCount("coordinator_segment_dropped_count", "Number of segments dropped due to being overshadowed", "service", "host", "version", "tier"),
        coordinatorSegmentDeletedCount("coordinator_segment_deleted_count", "Number of segments dropped due to rules", "service", "host", "version", "tier"),
        coordinatorSegmentUnneededCount("coordinator_segment_unneeded_count", "Number of segments dropped due to being marked as unused", "service", "host", "version", "tier"),
        coordinatorSegmentCostRaw("coordinator_segment_cost_count", "Used in cost balancing. The raw cost of hosting segments", "service", "host", "version", "tier"),
        coordinatorSegmentCostNormalization("coordinator_segment_cost_normalization", "Used in cost balancing. The normalization of hosting segments", "service", "host", "version", "tier"),
        coordinatorSegmentCostNormalized("coordinator_segment_cost_normalized", "Used in cost balancing. The normalized cost of hosting segments", "service", "host", "version", "tier"),
        coordinatorSegmentLoadQueueSize("coordinator_segment_load_queue_size", "Size in bytes of segments to load", "service", "host", "version", "server"),
        coordinatorSegmentLoadQueueFailed("coordinator_segment_load_queue_failed", "Number of segments that failed to load", "service", "host", "version", "server"),
        coordinatorSegmentLoadQueueCount("coordinator_segment_load_queue_count", "Number of segments to load", "service", "host", "version", "server"),
        coordinatorSegmentDropQueueCount("coordinator_segment_drop_queue_count", "Number of segments to drop", "service", "host", "version", "server"),
        coordinatorSegmentSize("coordinator_segment_size", "Size in bytes of available segments", "service", "host", "version", "dataSource"),
        coordinatorSegmentCount("coordinator_segment_count", "Number of available segments", "service", "host", "version", "dataSource"),
        coordinatorSegmentOverShadowedCount("coordinator_segment_over_shadowed_count", "Number of overShadowed segments", "service", "host", "version"),
        coordinatorSegmentUnavailableCount("coordinator_segment_unavailable_count", "Number of segments (not including replicas) left to load until segments that should be loaded in the cluster are available for queries", "service", "host", "version", "dataSource"),
        coordinatorSegmentUnderReplicatedCount("coordinator_segment_under_replicated_count", "Number of segments (including replicas) left to load until segments that should be loaded in the cluster are available for queries", "service", "host", "version", "dataSource", "tier"),

        //General Health
        //Historical
        historicalSegmentMax("historical_segment_max", "Maximum byte limit available for segments", "service", "host", "version"),
        historicalSegmentUsed("historical_segment_used", "Bytes used for served segments", "service", "host", "version", "dataSource", "tier", "priority"),
        historicalSegmentUsedPercent("historical_segment_used_percent", "Percentage of space used by served segments", "service", "host", "version", "dataSource", "tier", "priority"),
        historicalSegmentCount("historical_segment_count", "Number of served segments", "service", "host", "version", "dataSource", "tier", "priority"),
        historicalPendingDelete("historical_segment_pending_delete", "On-disk size in bytes of segments that are waiting to be cleared out", "service", "host", "version"),


        //JVM: These metrics are only available if the JVMMonitor module is included
        jvmPoolCommitted("jvm_pool_committed", "Committed pool", "service", "host", "version", "poolKind", "poolName"),
        jvmPoolInit("jvm_pool_init", "Initial pool", "service", "host", "version", "poolKind", "poolName"),
        jvmPoolMax("jvm_pool_max", "Max pool", "service", "host", "version", "poolKind", "poolName"),
        jvmPoolUsed("jvm_pool_used", "Pool used", "service", "host", "version", "poolKind", "poolName"),
        jvmBufferpoolCount("jvm_bufferpool_count", "Bufferpool count", "service", "host", "version", "bufferPoolName"),
        jvmBufferpoolUsed("jvm_bufferpool_used", "Bufferpool used", "service", "host", "version", "bufferPoolName"),
        jvmBufferpoolCapacity("jvm_bufferpool_capacity", "Bufferpool capacity", "service", "host", "version", "bufferPoolName"),
        jvmMemInit("jvm_mem_init", "Initial memory", "service", "host", "version", "memKind"),
        jvmMemMax("jvm_mem_max", "Max memory", "service", "host", "version", "memKind"),
        jvmMemUsed("jvm_mem_used", "Used memory", "service", "host", "version", "memKind"),
        jvmMemCommitted("jvm_mem_committed", "Committed memory", "service", "host", "version"),
        jvmGcCount("jvm_gc_count", "Garbage collection count", "service", "host", "version", "gcName"),
        jvmGcTime("jvm_gc_time", "Garbage collection time", "service", "host", "version", "gcName"),

        //EventReceiverFirehose: The following metric is only available if the EventReceiverFirehoseMonitor module is included.
        ingestEventsBuffered("ingest_events_buffered", "Number of events queued in the EventReceiverFirehose's buffer", "service", "host", "version", "dataSource", "serviceName"),
        ingestBytesReceived("ingest_bytes_received", "Number of bytes received by the EventReceiverFirehose", "service", "host", "version", "dataSource", "serviceName"),

        //Sys: These metrics are only available if the SysMonitor module is included.
        sysSwapFree("sys_swap_free", "Free swap", "service", "host", "version"),
        sysSwapMax("sys_swap_max", "Max swap", "service", "host", "version"),
        sysSwapPageIn("sys_swap_page_in", "Paged in swap", "service", "host", "version"),
        sysSwapPageOut("sys_swap_page_out", "Paged out swap", "service", "host", "version"),
        sysDiskWriteCount("sys_disk_write_count", "Writes to disk", "service", "host", "version"),
        sysDiskReadCount("sys_disk_read_count", "Reads from disk", "service", "host", "version"),
        sysDiskWriteSize("sys_disk_write_size", "Bytes written to disk. Can we used to determine how much paging is occuring with regards to segments", "service", "host", "version"),
        sysDiskReadSize("sys_disk_read_size", "Bytes read from disk. Can we used to determine how much paging is occuring with regards to segments", "service", "host", "version"),
        sysNetWriteSize("sys_net_write_size", "Bytes written to the network", "service", "host", "netName", "netAddress", "version"),
        sysNetReadSize("sys_net_read_size", "Bytes read from the network", "service", "host", "netName", "netAddress", "version"),
        sysFsUsed("sys_fs_used", "Filesystem bytes used", "service", "host", "version"),
        sysFsMax("sys_fs_max", "Filesystem bytes max", "service", "host", "version"),
        sysMemUsed("sys_mem_used", "Memory used", "service", "host", "version"),
        sysMemMax("sys_mem_max", "Memory max", "service", "host", "version"),
        sysStorageUsed("sys_storage_used", "Disk space used", "service", "host", "version"),
        sysCpu("sys_cpu", "CPU used", "service", "host", "version"),

        //unknown metric
        unknownMetric("unknown_metric", "Unknown metric", "feed", "metric", "service", "host", "version"),

        //monitoring node crash
        componentHeartBeats("component_heart_beats", "Monitoring component running", "service", "host", "version");


        String name;
        String help;
        String[] labelNames;

        Metric(String name, String help, String... labelNames) {
            this.name = name;
            this.help = help;
            this.labelNames = labelNames;
        }

        public String getName() {
            return this.name;
        }

        public String getHelp() {
            return this.help;
        }

        public String[] getLabels() {
            return this.labelNames.clone();
        }

    }

    public synchronized void start(int port) {
        if (initialized) {
            return;
        }
        try {
            // expose metrics with http service
            Server server = new Server(port);
            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            server.setHandler(context);
//            DefaultExports.initialize();
            context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
            context.addServlet(new ServletHolder(new DruidReportMetricsServlet()), "/");
            server.start();
            LOG.info("{} server port {} started", getTime(new Date()), port);
            initialized = true;
        } catch (Exception e) {
            LOG.error("Failed to create metric http service", e);
        }
    }

    public synchronized <T> T getMetric(Metric metric, Class<T> klass) {
        if (!initialized) {
            throw new RuntimeException("com.ebay.druid.MetricsReporter is not initialized yet");
        }
        if (metricsInCache.containsKey(metric)) {
            return (T) metricsInCache.get(metric);
        }
        T result = null;
        if (Gauge.class.isAssignableFrom(klass)) {
            result = (T) Gauge.build()
                    .name(metric.getName())
                    .help(metric.getHelp())
                    .labelNames(metric.getLabels())
                    .register(registry);
        } else if (Counter.class.isAssignableFrom(klass)) {
            result = (T) Counter.build()
                    .name(metric.getName())
                    .help(metric.getHelp())
                    .labelNames(metric.getLabels())
                    .register(registry);
        } else if (Histogram.class.isAssignableFrom(klass)) {
            result = (T) Histogram.build()
                    .name(metric.getName())
                    .help(metric.getHelp())
                    .labelNames(metric.getLabels())
                    .buckets(new double[]{.01, .05, .1, .5, 1, 2.5, 5, 7.5, 10, 20, 30})
                    .register(registry);
        } else if (Summary.class.isAssignableFrom(klass)) {
            result = (T) Summary.build()
                    .name(metric.getName())
                    .quantile(0.5, 0.05)
                    .quantile(0.9, 0.01)
                    .quantile(0.99, 0.001)
                    .help(metric.getHelp())
                    .labelNames(metric.getLabels())
                    .register(registry);
        }
        if (result != null) {
            metricsInCache.put(metric, (SimpleCollector) result);
            return result;
        }
        throw new IllegalArgumentException("Unknown metric type: " + klass.getName());
    }

    public synchronized Histogram getHistogramMetric(Metric metric, double... buckets) {
        if (!initialized) {
            throw new RuntimeException("com.ebay.druid.MetricsReporter is not initialized yet");
        }
        if (metricsInCache.containsKey(metric)) {
            return (Histogram) metricsInCache.get(metric);
        }
        Histogram result = Histogram.build()
                .name(metric.getName())
                .help(metric.getHelp())
                .labelNames(metric.getLabels())
                .buckets(buckets)
                .register(registry);
        metricsInCache.put(metric, result);
        return result;
    }

    public synchronized Histogram getHistogramTimeMetric(Metric metric) {
        double[] buckets = new double[]{50, 100, 500, 1000, 2500, 5000, 7500, 10000, 20000, 30000, 60000};
        return getHistogramMetric(metric, buckets);
    }

    public synchronized Histogram getHistogramBytesMetric(Metric metric) {
        double[] buckets = new double[]{2000, 40000, 6000, 8000, 10000, 20000, 40000, 60000, 80000, 100000, 500000, 1000000, 10000000};
        return getHistogramMetric(metric, buckets);
    }

    public synchronized Histogram getBrokerHistogramBytesMetric(Metric metric) {
        double[] buckets = new double[]{100, 200, 400, 600, 800, 1000, 1400, 1800, 2200, 4000, 6000, 10000, 30000, 50000, 100000};
        return getHistogramMetric(metric, buckets);
    }

}
