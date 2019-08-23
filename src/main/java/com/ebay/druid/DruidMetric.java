package com.ebay.druid;

import java.util.Map;

public class DruidMetric {
    //Common fields
    private String feed;
    protected String metric;
    private String timestamp;
    private String service;
    private String host;
    private String version;

    private Object value;

    //Broker, Historical, Real-time, Ingestion Metric
    private String dataSource;
    private String type;
    private String interval;
    private String remoteAddress;
    private String id;
    private String status;
    private String server;

    //Indexing Service
    private String taskType;
    private String taskStatus;

    //Coordination
    private String tier;

    //General Health
    private String priority;

    //JVM
    private String poolKind;
    private String poolName;
    private String bufferPoolName;
    private String memKind;
    private Object gcName;

    //EventReceiverFirehose
    private String serviceName;
    private String bufferCapacity;
    private String taskId;

    //Sys
    private String netName;
    private String netAddress;
    private String netHwaddr;
    private String cpuName;
    private String cpuTime;

    //when feed is alerts,like
    //{
    //    "feed": "alerts",
    //    "severity": "component-failure",
    //    "data": {
    //      "segment": "traceRaw_2019-07-22T07:45:00.000Z_2019-07-22T08:00:00.000Z_2019-07-22T07:44:21.161Z",
    //      "class": "io.druid.segment.realtime.appenderator.AppenderatorImpl",
    //      "count": 122
    //    },
    //    "service": "druid/overlord",
    //    "description": "dataSource[traceRaw] -- incremental persist failed",
    //    "host": "host-3296405.lvs02.dev.ebayc3.com:8100",
    //    "version": "0.12.2",
    //    "timestamp": "2019-07-22T07:50:29.606Z"
    //}
    private String severity;
    private Map<String, Object> data;
    private String description;


    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPoolKind() {
        return poolKind;
    }

    public void setPoolKind(String poolKind) {
        this.poolKind = poolKind;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public String getBufferPoolName() {
        return bufferPoolName;
    }

    public void setBufferPoolName(String bufferPoolName) {
        this.bufferPoolName = bufferPoolName;
    }

    public String getMemKind() {
        return memKind;
    }

    public void setMemKind(String memKind) {
        this.memKind = memKind;
    }

    public String getGcName() {
        if (gcName != null) {
            return gcName.toString();
        }
        return null;
    }

    public void setGcName(Object gcName) {
        this.gcName = gcName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getBufferCapacity() {
        return bufferCapacity;
    }

    public void setBufferCapacity(String bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getNetName() {
        return netName;
    }

    public void setNetName(String netName) {
        this.netName = netName;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public void setNetAddress(String netAddress) {
        this.netAddress = netAddress;
    }

    public String getNetHwaddr() {
        return netHwaddr;
    }

    public void setNetHwaddr(String netHwaddr) {
        this.netHwaddr = netHwaddr;
    }

    public String getCpuName() {
        return cpuName;
    }

    public void setCpuName(String cpuName) {
        this.cpuName = cpuName;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCpuTime() {
        return cpuTime;
    }

    public void setCpuTime(String cpuTime) {
        this.cpuTime = cpuTime;
    }
}
