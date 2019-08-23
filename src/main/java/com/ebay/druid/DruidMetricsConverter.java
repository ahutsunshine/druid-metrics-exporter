package com.ebay.druid;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.ebay.druid.MetricsReporter.Metric.*;
import static com.ebay.druid.MetricsReporter.metricMap;

public class DruidMetricsConverter {
    private static final Logger LOG = LoggerFactory.getLogger(DruidMetricsConverter.class);
    private static Map<String, Long> unknownMetricMap = new HashMap<>();
    private static Map<String, Long> unsupportedMetricTypeMap = new HashMap<>();

    public static void convertDruidMetric(String metricJson) {
        TypeReference<List<DruidMetric>> typeRef = new TypeReference<List<DruidMetric>>() {
        };
        List<DruidMetric> metrics;
        try {
            metrics = JSON.parseObject(metricJson, typeRef);
        } catch (Exception e) {
            LOG.error("Metric convert happens exception.", e);
            LOG.info("Metric json:\n{}", metricJson);
            return;
        }
        if (metrics == null) return;
        for (DruidMetric metric : metrics) {
            try {
                //make sure all needed metrics are registered in metricMap
                MetricsReporter.Metric metricEnum = getMetric(metric);
                if (metricEnum != null) {
                    String feed = metric.getFeed();
                    String service = metric.getService().replace("druid/", "");
                    String metricName = metric.getMetric() != null ? metric.getMetric().replaceAll("/", "_") : null;
                    String host = metric.getHost();
                    String version = metric.getVersion();
                    String dataSource = metric.getDataSource();
                    double value = 0;
                    //alert feed has no value and metric name
                    if (metric.getValue() != null) {
                        value = Double.parseDouble(String.valueOf(metric.getValue()));
                    }
                    switch (metricEnum) {
                        //counter type
                        case alertUnexpectedException:
                            MetricsReporter.getInstance().getMetric(metricEnum, Counter.class)
                                    .labels(service, host, version, metric.getSeverity()).inc();
                            break;
                        case unknownMetric:
                            MetricsReporter.getInstance().getMetric(metricEnum, Counter.class).labels(feed, metricName, service, host, version).inc();
                            break;
                        //Gauge type
                        case querySuccessCount:
                        case queryFailedCount:
                        case queryInterruptedCount:
                        case segmentScanPending:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(metricName, service, host, version).set(value);
                            break;
                        case queryCacheTotalNumEntries:
                        case queryCacheTotalSizeBytes:
                        case queryCacheTotalHits:
                        case queryCacheTotalMisses:
                        case queryCacheTotalEvictions:
                        case queryCacheTotalHitRate:
                        case queryCacheTotalAverageBytes:
                        case queryCacheTotalTimeouts:
                        case queryCacheTotalErrors:
                        case queryCacheDeltaNumEntries:
                        case queryCacheDeltaHits:
                        case queryCacheDeltaMisses:
                        case queryCacheDeltaEvictions:
                        case queryCacheDeltaTimeouts:
                        case queryCacheDeltaErrors:
                        case queryCacheDeltaSizeBytes:
                        case queryCacheDeltaAverageBytes:
                        case queryCacheDeltaHitRate:
                        case jvmMemCommitted:
//                        case sysSwapFree:
//                        case sysSwapMax:
//                        case sysSwapPageIn:
//                        case sysSwapPageOut:
//                        case sysDiskWriteCount:
//                        case sysDiskReadCount:
//                        case sysDiskWriteSize:
//                        case sysDiskReadSize:
//                        case sysFsUsed:
//                        case sysFsMax:
//                        case sysMemUsed:
//                        case sysMemMax:
//                        case sysStorageUsed:
                        case historicalSegmentMax:
                        case historicalPendingDelete:
                        case coordinatorSegmentOverShadowedCount:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version).set(value);
                            break;
//                        case sysNetWriteSize:
//                        case sysNetReadSize:
//                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, metric.getNetName(), metric.getNetAddress(), version).set(value);
//                            break;
                        case ingestEventsThrownAway:
                        case ingestEventsUnparseable:
                        case ingestEventsProcessed:
                        case ingestRowsOutput:
                        case ingestPersistsCount:
                        case ingestPersistsTime:
                        case ingestPersistsCpu:
                        case ingestPersistsBackPressure:
                        case ingestPersistsFailed:
                        case ingestHandoffFailed:
                        case ingestMergeTime:
                        case ingestMergeCpu:
                        case ingestHandoffCount:
                        case ingestSinkCount:
                        case ingestEventsMessageGap:
                        case ingestKafkaLag:
                        case coordinatorSegmentSize:
                        case coordinatorSegmentCount:
                        case coordinatorSegmentUnavailableCount:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, dataSource).set(value);
                            break;
                        case coordinatorSegmentAssignedCount:
                        case coordinatorSegmentMovedCount:
                        case coordinatorSegmentDroppedCount:
                        case coordinatorSegmentDeletedCount:
                        case coordinatorSegmentUnneededCount:
                        case coordinatorSegmentCostRaw:
                        case coordinatorSegmentCostNormalization:
                        case coordinatorSegmentCostNormalized:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getTier()).set(value);
                            break;
                        case coordinatorSegmentLoadQueueSize:
                        case coordinatorSegmentLoadQueueFailed:
                        case coordinatorSegmentLoadQueueCount:
                        case coordinatorSegmentDropQueueCount:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getServer()).set(value);
                            break;
                        case coordinatorSegmentUnderReplicatedCount:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, dataSource, metric.getTier()).set(value);
                            break;
                        case historicalSegmentUsed:
                        case historicalSegmentUsedPercent:
                        case historicalSegmentCount:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, dataSource, metric.getTier(), metric.getPriority()).set(value);
                            break;

                        case jvmPoolCommitted:
                        case jvmPoolInit:
                        case jvmPoolMax:
                        case jvmPoolUsed:
                            if (metricEnum.equals(jvmPoolUsed)) {
                                MetricsReporter.getInstance().getMetric(componentHeartBeats, Counter.class).labels(service, host, version).inc();
                            }
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getPoolKind(), metric.getPoolName()).set(value);
                            break;
                        case jvmBufferpoolCount:
                        case jvmBufferpoolUsed:
                        case jvmBufferpoolCapacity:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getBufferPoolName()).set(value);
                            break;
                        case jvmMemInit:
                        case jvmMemMax:
                        case jvmMemUsed:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getMemKind()).set(value);
                            break;
                        case jvmGcCount:
                        case jvmGcTime:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getGcName()).set(value);
                            break;
                        case ingestEventsBuffered:
                        case ingestBytesReceived:
                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version, metric.getDataSource(), metric.getServiceName()).set(value);
                            break;
//                        case sysCpu:
//                            MetricsReporter.getInstance().getMetric(metricEnum, Gauge.class).labels(service, host, version).set(value);
//                            break;
                        //Histogram type
                        case queryTime:
                            MetricsReporter.getInstance().getHistogramTimeMetric(metricEnum).
                                    labels(metricName, service, host, version, dataSource, metric.getType(), metric.getRemoteAddress()).observe(value);
                            break;
                        case brokerQueryBytes:
                            MetricsReporter.getInstance().getBrokerHistogramBytesMetric(metricEnum).
                                    labels(service, host, version, dataSource, metric.getType(), metric.getRemoteAddress()).observe(value);
                            break;
                        case queryWaitTime:
                            MetricsReporter.getInstance().getHistogramTimeMetric(metricEnum).labels(metricName, service, host, version).observe(value);
                            break;
                        case brokerQueryNodeTime:
                        case brokerQueryNodeTtfb:
                        case brokerQueryIntervalChunkTime:
                            MetricsReporter.getInstance().getHistogramTimeMetric(metricEnum).labels(service, host, version, metric.getServer()).observe(value);
                            break;
                        case brokerQueryNodeBytes:
                            MetricsReporter.getInstance().getBrokerHistogramBytesMetric(metricEnum).labels(service, host, version, metric.getServer()).observe(value);
                            break;
                        case historicalQuerySegmentTime:
                        case historicalQuerySegmentAndCacheTime:
                            MetricsReporter.getInstance().getHistogramTimeMetric(metricEnum).labels(service, host, version).observe(value);
                            break;
                        case historicalQueryCpuTime:
                            MetricsReporter.getInstance().getHistogramTimeMetric(metricEnum).labels(service, host, version,
                                    dataSource == null ? "default" : dataSource, metric.getType() == null ? "groupBy" : metric.getType()).observe(value);
                            break;
                        case jettyNumOpenConnections:
                            MetricsReporter.getInstance().getHistogramMetric(metricEnum, new double[]{5, 10, 20, 50, 100, 200, 500}).labels(service, host, version).observe(value);
                            MetricsReporter.getInstance().getMetric(jettyOpenConnectionsCount, Gauge.class).labels(service, host, version).set(value);
                            break;
                        case taskRunTime:
                            Object val = metric.getValue();
                            double duration = value;
                            String durationStatus = "1";//means task run time is more than 0.
                            if ((val instanceof Long) && Long.valueOf(String.valueOf(val)) == -1L) {
                                //FAILED ,duration = -1, make duration 1ms value
                                LOG.warn("Task run failed. Run time:{}, host:{}, dataSource:{}, taskType:{}, taskStatus:{}", value, host, dataSource, metric.getTaskType(), metric.getTaskStatus());
                                duration = 0;
                                durationStatus = "-1";//means task run time is -1.
                            }
                            MetricsReporter.getInstance().getHistogramMetric(metricEnum, new double[]{60 * 1000, 5 * 60 * 1000, 10 * 60 * 1000, 30 * 60 * 1000, 60 * 60 * 1000,
                                    2 * 60 * 60 * 1000, 3 * 60 * 60 * 1000, 5 * 60 * 60 * 1000, 24 * 60 * 60 * 1000})
                                    .labels(service, host, version, dataSource, metric.getTaskType(), metric.getTaskStatus()).observe(duration);

                            MetricsReporter.getInstance().getMetric(taskRunTimeCount, Counter.class).labels(service, host, version, dataSource, metric.getTaskType(), metric.getTaskStatus(), durationStatus).inc();
                            break;
                        case segmentAddedBytes:
                        case segmentMovedBytes:
                        case segmentNukedBytes:
                            MetricsReporter.getInstance().getHistogramBytesMetric(metricEnum).
                                    labels(service, host, version, dataSource, metric.getTaskType()).observe(value);
                            break;
                        default:
                            LOG.warn("Does not support such metric {} ", JSON.toJSONString(metric, true));
                            break;
                    }
                }
            } catch (Exception e) {
                LOG.error("Convert exception.", e);
                LOG.error(JSON.toJSONString(metric));
            }
        }
    }

    private static MetricsReporter.Metric getMetric(DruidMetric metric) {
        String feed = metric.getFeed();
        String metricName = metric.getMetric();
        String service = metric.getService();
        if (feed == null) return null;
        if ("metrics".equals(feed)) {
            if (metricName == null) return null;
            service = service.replace("druid/", "");
            if (metricName.equals("segment/count") && service.equals("coordinator")) {
                metricName = "coordinator/segment/count";
            } else if (metricName.equals("segment/count") && service.equals("historical")) {
                metricName = "historical/segment/count";
            }
            MetricsReporter.Metric m = metricMap.get(metricName);
            if (m == null) {
                long count = unknownMetricMap.getOrDefault(metricName, 0L);
                if (count % 1000 == 0) {
                    LOG.warn("Unknown metric:{}, feed:metrics, service:{}, count:{}.\n {}", metricName, metric.getService(), count, JSON.toJSONString(metric));
                }
                unknownMetricMap.put(metricName, count + 1);
            }
            return m != null ? m : unknownMetric;
        } else if ("alerts".equals(feed)) {
            LOG.error("Druid alerts occurs. Timestamp:{}, service:{}, host:{}, severity:{}, description:{}, data:{}",
                    metric.getTimestamp(), metric.getService(), metric.getHost(), metric.getSeverity(), metric.getDescription(), metric.getData().toString());
            return alertUnexpectedException;
        } else {
            long count = unsupportedMetricTypeMap.getOrDefault(feed, 0L);
            if (count % 1000 == 0) {
                LOG.warn("Metric report does not support {} type.  Count:{}.\n {}", feed, count, JSON.toJSONString(metric));
            }
            unsupportedMetricTypeMap.put(feed, count + 1);
        }
        return null;

    }
}
