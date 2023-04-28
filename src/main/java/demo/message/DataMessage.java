package demo.message;

public class DataMessage {
    private long id;
    private double humidity;
    private String timestamp;

    public DataMessage() {
    }

    public DataMessage(long id, double humidity, String timestamp) {
        this.id = id;
        this.humidity = humidity;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DataMessage{" +
                "id=" + id +
                ", humidity=" + humidity +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
