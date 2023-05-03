package com.uet.CoAPapi.message;

public class ControlMessage {
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final int TURN_ON = 0;
    public static final int TURN_OFF = 1;
    private String sensorId;
    private int option;
    private long delay;
    private String message;

    public ControlMessage() {
    }

    public ControlMessage(String sensorId, int option) {
        this.sensorId = sensorId;
        this.option = option;
        this.delay = -1;
        setMessageFromOption(this.option);
    }

    public ControlMessage(String sensorId, int option, long delay) {
        this.sensorId = sensorId;
        this.option = option;
        this.delay = delay;
        setMessageFromOption(this.option);
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
        setMessageFromOption(this.option);
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private void setMessageFromOption(int option) {
        if (option == 1) {
            this.message = OFF;
        } else {
            this.message = ON;
        }
    }

    @Override
    public String toString() {
        return "ControlMessage{" +
                "sensorId='" + sensorId + '\'' +
                ", option=" + option +
                ", message='" + message + '\'' +
                '}';
    }
}
