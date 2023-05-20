package com.uet.CoAPapi.coap.client;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "sensors")
public class Sensor {
    public static long DEFAULT_TIME_INTERVAL = -1;
    public static long DEFAULT_DELAY = 5000;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private long id;

    private String name;
    @Transient
    private long delay;
    @Transient
    private double humidity;
    @Transient
    private long timestamp;
    @Transient
    private boolean isUpdated;
    @Transient
    private Thread generateDataThread;
    @Transient
    private boolean isRunning;
    @Transient
    private boolean isAlive;
    @Transient
    private double throughput;

    public Sensor() {
    }

    public Sensor(double humidity) {
        this.name = "sensor-" + UUID.randomUUID();
        this.delay = DEFAULT_DELAY;
        this.humidity = humidity;
        this.timestamp = System.currentTimeMillis();
        this.isUpdated = false;
        this.isRunning = false;
        this.isAlive = true;
        this.throughput = 0;
    }
    public Sensor(String name, double humidity) {
        this.name = name;
        this.delay = DEFAULT_DELAY;
        this.humidity = humidity;
        this.timestamp = System.currentTimeMillis();
        this.isUpdated = false;
        this.isRunning = false;
        this.isAlive = true;
        this.throughput = 0;
    }

    public Sensor(String name, double humidity, long delay) {
        this.name = name;
        this.delay = delay;
        this.humidity = humidity;
        this.timestamp = System.currentTimeMillis();
        this.isUpdated = false;
        this.isRunning = false;
        this.isAlive = true;
        this.throughput = 0;
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

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
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

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", delay=" + delay +
                ", humidity=" + humidity +
                ", timestamp=" + timestamp +
                ", isUpdated=" + isUpdated +
                ", isRunning=" + isRunning +
                '}';
    }

    public void loadInitData() {
        this.delay = DEFAULT_DELAY;
        this.humidity = (new Random()).nextDouble();
        this.timestamp = System.currentTimeMillis();
        this.isUpdated = false;
        this.isRunning = false;
        this.isAlive = true;
        this.throughput = 0;
    }

    public void startGenerateData() {
        generateDataThread = new Thread(new DataGenerator(this));
        generateDataThread.start();
    }

    public void startGenerateData(long timeInterval, long delay) {
        this.setDelay(delay);
        generateDataThread = new Thread(new DataGenerator(this, timeInterval));
        generateDataThread.start();
    }

    public static void main(String[] args) {
        Sensor sensor = new Sensor(0.5);
        sensor.startGenerateData(2000, 500);
    }
}
