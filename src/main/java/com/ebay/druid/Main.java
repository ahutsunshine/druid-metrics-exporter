package com.ebay.druid;

public class Main {
    public static void main(String[] args) {
        int port = 9001;
        if (args != null && args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        MetricsReporter.getInstance().start(port);
    }
}
