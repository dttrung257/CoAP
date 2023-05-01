package demo.client;

import java.util.Date;

public class Sensor {
    private static long count = 0;
    protected static long DEFAULT_TIME_INTERVAL = 5000;
    protected static long DEFAULT_DELAY = 1000;
    private long id;
    private String name;
    private double humidity;
    private Date timestamp;
    private boolean isUpdated;
    private Thread generateDataThread;
    private boolean isRunning;
    public Sensor() {
    }

    public Sensor(double humidity, Date timestamp) {
        this.id = count++;
        this.name = "sensor-" + id;
        this.humidity = humidity;
        this.timestamp = timestamp;
        this.isUpdated = false;
        this.isRunning = true;
    }
    public Sensor(String name, double humidity, Date timestamp) {
        this.id = count++;
        this.name = name;
        this.humidity = humidity;
        this.timestamp = timestamp;
        this.isUpdated = false;
        this.isRunning = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", humidity=" + humidity +
                ", timestamp=" + timestamp +
                ", isUpdated=" + isUpdated +
                ", generateDataThread=" + generateDataThread +
                ", isRunning=" + isRunning +
                '}';
    }

    public void startGenerateData() {
        generateDataThread = new Thread(new DataGenerator(this));
        generateDataThread.start();
    }

    public void startGenerateData(long timeInterval) {
        generateDataThread = new Thread(new DataGenerator(this, timeInterval));
        generateDataThread.start();
    }

    public void startGenerateData(long timeInterval, long delay) {
        generateDataThread = new Thread(new DataGenerator(this, timeInterval, delay));
        generateDataThread.start();
    }

    public static void main(String[] args) {
        Sensor sensor = new Sensor(0.5, new Date());
        sensor.startGenerateData(2000, 500);
    }
}
