package com.example.firealarm_iot;

public class Event {
    private String startTime;
    private String endTime;
    private int duration;
    private boolean flameDetected;
    private int gasValue;
    private double temperature;

    // Constructor
    public Event(String startTime, String endTime, int duration, boolean flameDetected, int gasValue, double temperature) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.flameDetected = flameDetected;
        this.gasValue = gasValue;
        this.temperature = temperature;
    }

    // Getter and Setter methods
    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isFlameDetected() {
        return flameDetected;
    }

    public int getGasValue() {
        return gasValue;
    }

    public double getTemperature() {
        return temperature;
    }
}
