#FROM deopcard.corp.hkjc.com/ai-msp-docker-release-local/airecw/rm/scoring/env:1.4
FROM deopcard.corp.hkjc.com/ai-dsw-docker-release-local/mapr-client-java:v0.2

RUN apt-get update -y && \
    apt-get -yqq upgrade && \
    apt-get install systemd -yqq && \
    apt-get install cron -yqq && \
    apt-get install vim -yqq && \
    export DEBIAN_FRONTEND=noninteractive && \
    apt-get install krb5-user -yqq

COPY out/artifacts/BetlineRecommendation_jar /usr/src/BetlineRecommendation_jar
COPY data /user/src/BetlineRecommendation_jar/data
COPY SolaceGSSConfig /usr/src/BetlineRecommendation_jar/SolaceGSSConfig
COPY SolaceGSSConfig/krb5.conf /etc

WORKDIR /usr/src/BetlineRecommendation_jar
#deopcard.corp.hkjc.com/ai-dsw-docker-release-local/airecw/rm/scoring/app:1.134