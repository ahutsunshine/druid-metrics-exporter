package com.ebay.druid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

import static com.ebay.druid.util.TimeUtil.getTime;

public class DruidReportMetricsServlet extends HttpServlet {
    private static final Logger LOG = LoggerFactory.getLogger(DruidReportMetricsServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("time:{}, method:{}, port:{}, redirect to MetricsServlet", getTime(new Date()), req.getMethod(), req.getServerPort());
        RequestDispatcher dispatcher = req.getRequestDispatcher("metrics");
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("time:{}, method:{}, port:{}, path:{}", getTime(new Date()), req.getMethod(), req.getServerPort(), req.getServletPath());
        String metricJson = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String contentType = req.getHeader("Content-Type");
        if (contentType == null) {
            LOG.info("Content-Type is null, ignore it.");
        } else if (contentType.equalsIgnoreCase("application/json") && "/".equals(req.getServletPath())) {
            DruidMetricsConverter.convertDruidMetric(metricJson);
        } else {
            LOG.info("Content-Type is {}, path is {}, ignore it.", contentType, req.getServletPath());
        }

    }
}
