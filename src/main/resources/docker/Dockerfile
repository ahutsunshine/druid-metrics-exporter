FROM openjdk:8u102-jre

MAINTAINER deyiyao "deyiyao@ebay.com"

RUN mkdir /druid

EXPOSE 9001

ADD ./druid-metrics.jar /druid/

WORKDIR /druid

ENTRYPOINT java -jar druid-metrics.jar