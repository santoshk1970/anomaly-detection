FROM flink:1.18.1-scala_2.12-java17

# Copy the built JAR to Flink's lib directory
COPY target/anomaly-detection-1.0.0.jar /opt/flink/usrlib/anomaly-detection.jar

# Set working directory
WORKDIR /opt/flink

# The Flink base image already has the entry point configured
# Jobs will be submitted via Flink CLI or REST API
