package demo.message;

public class ControlMessage {
    public static final String STOP = "STOP";
    public static final String START = "START";
    public static final String RESUME = "RESUME";
    private String sensorId;
    private int option;
    private String message;

    public ControlMessage() {
    }

    public ControlMessage(String sensorId, int option) {
        this.sensorId = sensorId;
        this.option = option;
        setMessageFromOption(this.option);
    }

    public ControlMessage(String sensorId, int option, String message) {
        this.sensorId = sensorId;
        this.option = option;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private void setMessageFromOption(int option) {
        switch (option) {
            case 1 -> {
                this.message = STOP;
            }
            case 2 -> {
                this.message = RESUME;
            }
            default -> {
                this.message = START;
            }
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
