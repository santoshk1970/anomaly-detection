package com.grid.sinks;

import com.grid.events.SensorReading;
import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.formats.parquet.avro.AvroParquetWriters;
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.OnCheckpointRollingPolicy;

/**
 * Creates a Flink sink to write SensorReading events to an S3-compatible object store
 * in Parquet format.
 */
public class S3ParquetSink {

    public static FileSink<SensorReading> create(String s3Path) {
        return FileSink
                .forBulkFormat(new Path(s3Path), AvroParquetWriters.forSpecificRecord(SensorReading.class))
                .withBucketAssigner(new org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner<>("'year='yyyy'/month='MM'/day='dd'"))
                .withRollingPolicy(OnCheckpointRollingPolicy.build())
                .build();
    }
}
