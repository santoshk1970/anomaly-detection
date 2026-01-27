package com.grid.functions;

import com.grid.events.SensorReading;
import com.grid.model.AnomalyEventPojo;
import com.grid.model.SensorReadingPojo;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Detects anomalies based on voltage deviation or equipment status.
 */
public class AnomalyDetectorFunction extends ProcessFunction<Tuple3<SensorReading, Double, Double>, AnomalyEventPojo> {

    private final double threshold;

    public AnomalyDetectorFunction(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public void processElement(Tuple3<SensorReading, Double, Double> value, Context ctx, Collector<AnomalyEventPojo> out) throws Exception {
        SensorReading reading = value.f0;
        Double mean = value.f1;
        Double stdDev = value.f2;

        double deviation = Math.abs(reading.getVoltage() - mean);
        
        // Convert SensorReading to POJO
        SensorReadingPojo readingPojo = new SensorReadingPojo();
        readingPojo.setMeterId(reading.getMeterId().toString());
        readingPojo.setTimestamp(reading.getTimestamp());
        readingPojo.setVoltage(reading.getVoltage());
        readingPojo.setCurrent(reading.getCurrent());
        readingPojo.setTemperature(reading.getTemperature());
        if (reading.getStatus() != null) {
            readingPojo.setStatus(reading.getStatus().toString());
        }

        // Check for voltage spike anomaly
        if (stdDev > 0 && deviation > (threshold * stdDev)) {
            AnomalyEventPojo anomaly = new AnomalyEventPojo(
                    "Voltage Spike",
                    "ERROR",
                    readingPojo,
                    threshold,
                    deviation
            );
            out.collect(anomaly);
            return; // Avoid creating duplicate anomalies
        }

        // Check for equipment failure status anomaly
        if (reading.getStatus() != null && reading.getStatus().toString().contains("FAILURE")) {
            AnomalyEventPojo anomaly = new AnomalyEventPojo(
                    "Equipment Failure",
                    "CRITICAL",
                    readingPojo,
                    null,
                    null
            );
            out.collect(anomaly);
        }
    }
}
