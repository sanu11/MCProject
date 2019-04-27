package com.example.projectapp;

public class Sample
{
    private float heartbeat;
    private float variance;
    private String label;

    public Sample(float heartbeat, float variance, String label) {
        this.heartbeat = heartbeat;
        this.variance = variance;
        this.label = label;
    }

    public float getHeartrate() {
        return heartbeat;
    }
    public void setHeartrate(float heartbeat) {
        this.heartbeat = heartbeat;
    }
    public float getVariance() {
        return variance;
    }
    public void setVariance(float variance) {
        this.variance = variance;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "Sample [variance=" + variance + ", Label=" + label + "]";
    }
}
