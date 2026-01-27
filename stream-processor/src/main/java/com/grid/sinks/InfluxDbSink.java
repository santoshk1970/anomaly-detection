package com.grid.sinks;

import com.grid.events.AnomalyEvent;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

/**
 * A Flink sink to write AnomalyEvent data to InfluxDB.
 */
public class InfluxDbSink extends RichSinkFunction<AnomalyEvent> {

    private transient InfluxDBClient influxDBClient;
    private transient WriteApiBlocking writeApi;

    private final String url = "http://influxdb:8086";
    private final String token = "grid-admin-token";
    private final String org = "grid";
    private final String bucket = "grid";

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        influxDBClient = InfluxDBClientFactory.create(url, token.toCharArray(), org, bucket);
        writeApi = influxDBClient.getWriteApiBlocking();
    }

    @Override
    public void invoke(AnomalyEvent value, Context context) throws Exception {
        Point point = Point.measurement("anomalies")
                .addTag("meter_id", value.getOriginalEvent().getMeterId().toString())
                .addTag("severity", value.getSeverity().toString())
                .addField("anomaly_type", value.getAnomalyType().toString())
                .addField("voltage", value.getOriginalEvent().getVoltage())
                .time(value.getOriginalEvent().getTimestamp(), WritePrecision.MS);

        writeApi.writePoint(point);
    }

    @Override
    public void close() throws Exception {
        super.close();
        if (influxDBClient != null) {
            influxDBClient.close();
        }
    }
}
