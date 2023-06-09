package com.uet.CoAPapi.coap.message;

public class ControlMessage {
    public static final String TURN_ON_MESSAGE = "ON";
    public static final String TURN_OFF_MESSAGE = "OFF";
    public static final String TERMINATE_MESSAGE = "TERMINATE";
    private static final String OTHER_MESSAGE = "OTHER";
    public static final int TURN_ON_OPTION = 0;
    public static final int TURN_OFF_OPTION = 1;
    public static final int TERMINATE_OPTION = 2;
    private static final int OTHER_OPTION = 3;
    private String sensorId;
    private int option;
    private long delay;
    private String message;
    private long timestamp;

    public ControlMessage() {
    }

    public ControlMessage(String sensorId, int option) {
        this(sensorId, option, -1);
    }

    public ControlMessage(String sensorId, long delay) {
        this(sensorId, OTHER_OPTION, delay);
    }

    private ControlMessage(String sensorId, int option, long delay) {
        this.sensorId = sensorId;
        this.option = option;
        this.delay = delay;
        setMessageFromOption(this.option);
        this.timestamp = System.currentTimeMillis();
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

    public long getTimestamp() {
        return timestamp;
    }

    private void setMessageFromOption(int option) {
        switch (option) {
            case TURN_ON_OPTION -> this.message = TURN_ON_MESSAGE;
            case TURN_OFF_OPTION -> this.message = TURN_OFF_MESSAGE;
            case TERMINATE_OPTION -> this.message = TERMINATE_MESSAGE;
            default -> this.message = OTHER_MESSAGE;
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