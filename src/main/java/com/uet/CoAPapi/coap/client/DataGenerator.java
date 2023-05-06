package com.uet.CoAPapi.coap.client;


import java.util.Date;
import java.util.Random;

public class DataGenerator implements Runnable {
    private final Sensor sensor;
    private long timeInterval = Sensor.DEFAULT_TIME_INTERVAL;

    static final double MIN_AVERAGE_HUMIDITY = 0.81; // In Hanoi
    static final double MAX_AVERAGE_HUMIDITY = 0.89; // In Hanoi

    public DataGenerator(Sensor sensor) {
        this.sensor = sensor;
    }

    public DataGenerator(Sensor sensor, long timeInterval) {
        this.sensor = sensor;
        this.timeInterval = timeInterval;
    }

    /*
      Average humidity in Hanoi : 81% (min) - 89% (max)
     */
    private static double humidityGenerate() {
        double random = (new Random()).nextDouble();
        return MIN_AVERAGE_HUMIDITY + (MAX_AVERAGE_HUMIDITY - MIN_AVERAGE_HUMIDITY) * random;
    }

    @Override
    public void run() {
        while (true) {
            if (this.timeInterval > 0) {
                final Date endTime = (new Date(System.currentTimeMillis() + this.timeInterval));
                while ((new Date(System.currentTimeMillis())).before(endTime) && this.sensor.isRunning()) {
                    this.sensor.setHumidity(humidityGenerate());
                    this.sensor.setTimestamp(new Date());
                    this.sensor.setUpdated(true);
                    // System.out.println(this.sensor);
                    try {
                        Thread.sleep(this.sensor.getDelay());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (this.timeInterval == Sensor.DEFAULT_TIME_INTERVAL) {
                while (this.sensor.isRunning()) {
                    this.sensor.setHumidity(humidityGenerate());
                    this.sensor.setTimestamp(new Date());
                    this.sensor.setUpdated(true);
                    // System.out.println(this.sensor);
                    try {
                        Thread.sleep(this.sensor.getDelay());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
