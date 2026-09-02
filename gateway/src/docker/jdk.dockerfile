FROM eclipse-temurin:17-jdk-jammy
MAINTAINER lijiajia

WORKDIR /cairo/gateway

# jar包复制
COPY libs/*-boot.jar app.jar

COPY dist ./

RUN mkdir -p logs arthas-output dump

ENV JAVA_OPTS '\
-XX:+PrintFlagsFinal -XshowSettings:vm \
-XX:+UseContainerSupport -XX:+UnlockExperimentalVMOptions \
-XX:+HeapDumpOnOutOfMemoryError -XX:+ExitOnOutOfMemoryError -XX:+CrashOnOutOfMemoryError \
-XX:+UseZGC -XX:ZCollectionInterval=120 -XX:ZAllocationSpikeTolerance=5 \
-XX:+UnlockDiagnosticVMOptions -XX:-ZProactive \
'

#ENV JVM_OPTS '-server -XX:InitialRAMPercentage=70.0 -XX:MaxRAMPercentage=70.0 -Xss512K '
ENV JVM_OPTS '-server -XX:MaxRAMPercentage=70.0 -Xss512K '

#ENTRYPOINT ["sh", "-c","exec java -XX:HeapDumpPath=dump/dump-$(date '+%Y%m%d%H%m%S').hprof $JAVA_OPTS $JVM_OPTS org.springframework.boot.loader.JarLauncher"]
ENTRYPOINT ["sh", "-c","exec java $JAVA_OPTS $JVM_OPTS -jar app.jar"]

EXPOSE 80/tcp 9000/tcp

HEALTHCHECK --start-period=1m \
--interval=1m \
--timeout=10s \
--retries=3 \
CMD curl --silent --fail-early --request GET http://localhost:9000/actuator/health/liveness | jq --exit-status '.status == "UP"' || exit 1
