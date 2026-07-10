FROM maven:3.9.11-eclipse-temurin-11 AS build

WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -Dmaven.test.skip=true

FROM ghcr.io/eclipse-ee4j/glassfish:7.0.25

USER root
RUN mkdir -p /deploy /opt/glassfish7/custom \
    && chown -R glassfish:glassfish /deploy /opt/glassfish7/custom

COPY --from=build --chown=glassfish:glassfish \
    /workspace/target/AuctionSystem.war /deploy/AuctionSystem.war
COPY --chown=glassfish:glassfish docker/heroku-init.sh \
    /opt/glassfish7/custom/init.sh

RUN chmod 0755 /opt/glassfish7/custom/init.sh

USER glassfish

RUN asadmin start-domain domain1 \
    && asadmin create-jms-resource \
        --restype jakarta.jms.ConnectionFactory \
        jms/ConnectionFactory \
    && asadmin create-jms-resource \
        --restype jakarta.jms.Topic \
        jms/topic/BidUpdates \
    && asadmin deploy \
        --name AuctionSystem \
        /deploy/AuctionSystem.war \
    && asadmin stop-domain domain1

EXPOSE 8080
