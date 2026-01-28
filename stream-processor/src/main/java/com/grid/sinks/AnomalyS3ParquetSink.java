package com.grid.sinks;

import com.grid.model.AnomalyEventPojo;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.api.common.serialization.SimpleStringEncoder;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

/**
 * Creates a Flink sink to write AnomalyEventPojo events to MinIO
 * in JSON format for long-term storage and analytics.
 */
public class AnomalyS3ParquetSink {

    public static FileSink<String> create(String s3Path) {
        // Create JSON string writer
        return FileSink
                .forRowFormat(new Path(s3Path), new SimpleStringEncoder<String>("UTF-8"))
                .withBucketAssigner(new org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner<>("yyyy-MM-dd-HH"))
                .withRollingPolicy(OnCheckpointRollingPolicy.build())
                .build();
    }
}
