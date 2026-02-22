#build image ; 
docker build -t jms-otel-demo:1.0.0 .



#run publisher - pake main class \
override lg buat mastiin \
docker run --rm \
  -e MAIN_CLASS=com.example.JmsPublisher \
  -e SOLACE_HOST=tcp://host.docker.internal:55557 \
  -e SOLACE_VPN=default \
  -e SOLACE_USERNAME=default \
  -e SOLACE_PASSWORD=default \
  -e TOPIC_NAME=demo/otel/jms \
  -e MSG_COUNT=10 \
  -e JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar -Dotel.javaagent.extensions=/otel/solace-jms-otel-ext.jar -Dotel.propagators=solace_jms_tracecontext -Dotel.instrumentation.jms.enabled=true -Dotel.exporter.otlp.endpoint=http://host.docker.internal:4317 -Dotel.exporter.otlp.traces.protocol=grpc -Dotel.traces.exporter=otlp -Dotel.metrics.exporter=none -Dotel.logs.exporter=none -Dotel.resource.attributes=service.name=jms-publisher" \
  jms-otel-demo:1.0.0

#consumer \ 

docker run --rm \
  -e MAIN_CLASS=com.example.JmsConsumer \
  -e SOLACE_HOST=tcp://host.docker.internal:55557 \
  -e SOLACE_VPN=default \
  -e SOLACE_USERNAME=default \
  -e SOLACE_PASSWORD=default \
  -e QUEUE_NAME=Q.DEMO.OTEL.JMS \
  -e JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar -Dotel.javaagent.extensions=/otel/solace-jms-otel-ext.jar -Dotel.propagators=solace_jms_tracecontext -Dotel.instrumentation.jms.enabled=true -Dotel.exporter.otlp.endpoint=http://host.docker.internal:4317 -Dotel.exporter.otlp.traces.protocol=grpc -Dotel.traces.exporter=otlp -Dotel.metrics.exporter=none -Dotel.logs.exporter=none -Dotel.resource.attributes=service.name=jms-consumer" \
  jms-otel-demo:1.0.0
