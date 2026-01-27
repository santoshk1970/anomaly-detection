package com.grid.model;

import java.time.Instant;

public class AnomalyEventPojo {
    private String anomalyType;
    private String severity;
    private SensorReadingPojo originalEvent;
    private Double thresholdUsed;
    private Double deviation;
    
    public AnomalyEventPojo() {}
    
    public AnomalyEventPojo(String anomalyType, String severity, SensorReadingPojo originalEvent, Double thresholdUsed, Double deviation) {
        this.anomalyType = anomalyType;
        this.severity = severity;
        this.originalEvent = originalEvent;
        this.thresholdUsed = thresholdUsed;
        this.deviation = deviation;
    }
    
    // Getters and setters
    public String getAnomalyType() {
        return anomalyType;
    }
    
    public void setAnomalyType(String anomalyType) {
        this.anomalyType = anomalyType;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public SensorReadingPojo getOriginalEvent() {
        return originalEvent;
    }
    
    public void setOriginalEvent(SensorReadingPojo originalEvent) {
        this.originalEvent = originalEvent;
    }
    
    public Double getThresholdUsed() {
        return thresholdUsed;
    }
    
    public void setThresholdUsed(Double thresholdUsed) {
        this.thresholdUsed = thresholdUsed;
    }
    
    public Double getDeviation() {
        return deviation;
    }
    
    public void setDeviation(Double deviation) {
        this.deviation = deviation;
    }
}
