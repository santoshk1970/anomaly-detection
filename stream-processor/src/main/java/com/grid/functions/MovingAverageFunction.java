package com.grid.functions;

import com.grid.events.SensorReading;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

/**
 * Calculates a moving average and standard deviation for sensor voltage readings.
 */
public class MovingAverageFunction extends KeyedProcessFunction<CharSequence, SensorReading, Tuple3<SensorReading, Double, Double>> {

    // State for calculating moving average and standard deviation
    private transient ValueState<Long> count;
    private transient ValueState<Double> sum;
    private transient ValueState<Double> sumOfSquares;

    @Override
    public void open(Configuration parameters) throws Exception {
        count = getRuntimeContext().getState(new ValueStateDescriptor<>("count", Long.class));
        sum = getRuntimeContext().getState(new ValueStateDescriptor<>("sum", Double.class));
        sumOfSquares = getRuntimeContext().getState(new ValueStateDescriptor<>("sumOfSquares", Double.class));
    }

    @Override
    public void processElement(SensorReading reading, Context ctx, Collector<Tuple3<SensorReading, Double, Double>> out) throws Exception {
        // Initialize state if it's the first reading for this key
        Long currentCount = count.value();
        if (currentCount == null) {
            currentCount = 0L;
        }
        Double currentSum = sum.value();
        if (currentSum == null) {
            currentSum = 0.0;
        }
        Double currentSumOfSquares = sumOfSquares.value();
        if (currentSumOfSquares == null) {
            currentSumOfSquares = 0.0;
        }

        // Update state with the new reading
        currentCount++;
        currentSum += reading.getVoltage();
        currentSumOfSquares += reading.getVoltage() * reading.getVoltage();

        count.update(currentCount);
        sum.update(currentSum);
        sumOfSquares.update(currentSumOfSquares);

        // Calculate moving average and standard deviation
        double mean = currentSum / currentCount;
        double variance = (currentSumOfSquares / currentCount) - (mean * mean);
        // Ensure variance is non-negative due to potential floating point inaccuracies
        double stdDev = variance > 0 ? Math.sqrt(variance) : 0.0;

        // Emit the original reading along with the calculated statistics
        out.collect(new Tuple3<>(reading, mean, stdDev));
    }
}
