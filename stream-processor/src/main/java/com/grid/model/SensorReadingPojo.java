package com.grid.model;

import java.time.Instant;

public class SensorReadingPojo {
    private String meterId;
    private Instant timestamp;
    private double voltage;
    private double current;
    private double temperature;
    private String status;
    
    public SensorReadingPojo() {}
    
    public SensorReadingPojo(String meterId, Instant timestamp, double voltage, double current, double temperature, String status) {
        this.meterId = meterId;
        this.timestamp = timestamp;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
        this.status = status;
    }
    
    // Getters and setters
    public String getMeterId() {
        return meterId;
    }
    
    public void setMeterId(String meterId) {
        this.meterId = meterId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    public double getVoltage() {
        return voltage;
    }
    
    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }
    
    public double getCurrent() {
        return current;
    }
    
    public void setCurrent(double current) {
        this.current = current;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
