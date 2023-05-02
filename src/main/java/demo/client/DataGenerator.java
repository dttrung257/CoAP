package demo.client;

import java.util.Date;
import java.util.Random;

public class DataGenerator implements Runnable {
    private final Sensor sensor;
    private long timeInterval = Sensor.DEFAULT_TIME_INTERVAL;
    private long delay = Sensor.DEFAULT_DELAY;

    public DataGenerator(Sensor sensor) {
        this.sensor = sensor;
    }

    public DataGenerator(Sensor sensor, long delay) {
        this.sensor = sensor;
        this.delay = delay;
    }

    public DataGenerator(Sensor sensor, long timeInterval, long delay) {
        this.sensor = sensor;
        this.timeInterval = timeInterval;
        this.delay = delay;
    }

    @Override
    public void run() {
        while (true) {
            if (this.timeInterval > 0) {
                final Date endTime = (new Date(System.currentTimeMillis() + this.timeInterval));
                while ((new Date(System.currentTimeMillis())).before(endTime) && this.sensor.isRunning()) {
                    this.sensor.setHumidity((new Random()).nextDouble());
                    this.sensor.setTimestamp(new Date());
                    this.sensor.setUpdated(true);
                    // System.out.println(this.sensor);
                    try {
                        Thread.sleep(this.delay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else if (this.timeInterval == Sensor.DEFAULT_TIME_INTERVAL) {
                while (this.sensor.isRunning()) {
                    this.sensor.setHumidity((new Random()).nextDouble());
                    this.sensor.setTimestamp(new Date());
                    this.sensor.setUpdated(true);
                    // System.out.println(this.sensor);
                    try {
                        Thread.sleep(this.delay);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
