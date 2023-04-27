package demo.client;

public class Sensor {
    private static long COUNT = 0;
    private Long id;
    private double humidity;
    private long batteryLife;

    public Sensor() {
    }

    public Sensor(double humidity, long batteryLife) {
        this.id = COUNT++;
        this.humidity = humidity;
        this.batteryLife = batteryLife;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public long getBatteryLife() {
        return batteryLife;
    }

    public void setBatteryLife(long batteryLife) {
        this.batteryLife = batteryLife;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id=" + id +
                ", humidity=" + humidity +
                ", batteryLife=" + batteryLife +
                '}';
    }
}
