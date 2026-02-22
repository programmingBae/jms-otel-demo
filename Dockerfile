# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /src

COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests clean package

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# app jar from shade plugin
COPY --from=build /src/target/app.jar /app/app.jar

# OTel agent + Solace JMS extension
RUN mkdir -p /otel
COPY otel/opentelemetry-javaagent-1.29.0.jar /otel/opentelemetry-javaagent.jar
COPY otel/solace-opentelemetry-jms-integration-1.2.0.jar /otel/solace-jms-otel-ext.jar

# Choose which main class to run
ENV MAIN_CLASS="com.example.JmsPublisher"

# OTel config (recommended override in K8s)
ENV JAVA_TOOL_OPTIONS="\
-javaagent:/otel/opentelemetry-javaagent.jar \
-Dotel.javaagent.extensions=/otel/solace-jms-otel-ext.jar \
-Dotel.propagators=solace_jms_tracecontext \
-Dotel.instrumentation.jms.enabled=true \
-Dotel.exporter.otlp.endpoint=http://otel-collector:4317 \
-Dotel.exporter.otlp.traces.protocol=grpc \
-Dotel.traces.exporter=otlp \
-Dotel.metrics.exporter=none \
-Dotel.logs.exporter=none \
-Dotel.resource.attributes=service.name=jms-demo \
"

ENTRYPOINT ["sh","-c","java -cp /app/app.jar $MAIN_CLASS"]