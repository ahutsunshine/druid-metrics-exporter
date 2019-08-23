package com.ebay.druid;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.prometheus.client.Gauge;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.ebay.druid.MetricsReporter.Metric.historicalPendingDelete;

public class Test {
    private static final Logger LOGGER = Logger.getLogger(Test.class);
    public static void main(String[] args) {
        String content = readStringFromResource("test.json");
        TypeReference<List<DruidMetric>> typeRef = new TypeReference<List<DruidMetric>>() {
        };
        List<DruidMetric> metrics = JSON.parseObject(content, typeRef);
        System.out.println(metrics.size());

        MetricsReporter.getInstance().getMetric(historicalPendingDelete, Gauge.class).labels("", "", "").set(-1);


    }
    public static InputStream readFromResource(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            return inputStream;

        } catch (IOException e) {
            LOGGER.error("Read " + fileName + " from resource failure. ", e);
        }
        return null;
    }

    public static String readStringFromResource(String fileName) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        InputStream inputStream;
        try {
            inputStream = classLoader.getResourceAsStream(fileName);
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            LOGGER.error("Read " + fileName + " from resource failure. ", e);
        }
        return null;
    }
}
