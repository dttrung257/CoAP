package com.uet.CoAPapi.coap.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.message.DataMessage;
import com.uet.CoAPapi.mappers.MessageMapper;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;

public class Client {
    private final Sensor sensor;
    private final Thread listenThread;
    private final Thread sendDataThread;
    private CoapClient client;
    private long timeInterval = Sensor.DEFAULT_TIME_INTERVAL;
    private long delay = Sensor.DEFAULT_DELAY;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MessageMapper messageMapper = new MessageMapper();

    public Client(final Sensor sensor) {
        this.sensor = sensor;
        this.listenThread = new Thread(this::connectToGateway);
        this.sendDataThread = new Thread(this::sendData);
    }

    public Sensor getSensor() {
        return sensor;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    private static class ClientListener implements CoapHandler {
        private final Client client;

        private ClientListener(Client client) {
            this.client = client;
        }

        private void start() {
            this.client.sensor.setRunning(true);
            if (!this.client.sendDataThread.isAlive()) {
                this.client.sendDataThread.start();
            }
        }

        private void stop() {
            this.client.sensor.setRunning(false);
        }

        private void terminate() {
            this.client.sensor.setAlive(false);
        }

        @Override
        public void onLoad(CoapResponse response) {
            // System.out.println("Connection is created");
            if (response.getPayload().length > 0 && response.getPayload() != null) {
                try {
                    ControlMessage controlMessage = mapper.readValue(response.getPayload(), ControlMessage.class);
                    if (controlMessage.getSensorId().equalsIgnoreCase("ALL")
                            || Integer.parseInt(controlMessage.getSensorId()) == this.client.sensor.getId()) {
                        if (controlMessage.getDelay() != this.client.getSensor().getDelay()
                                && controlMessage.getDelay() > 0) {
                            this.client.setDelay(controlMessage.getDelay());
                            this.client.getSensor().setDelay(controlMessage.getDelay());
                        } else if (controlMessage.getDelay() == -1) {
                            String message = controlMessage.getMessage();
                            switch (message) {
                                case ControlMessage.TURN_ON_MESSAGE -> start();
                                case ControlMessage.TURN_OFF_MESSAGE -> stop();
                                case ControlMessage.TERMINATE_MESSAGE -> terminate();
                                default -> {
                                    System.out.println("Unknown message: " + message);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onError() {
            System.err.println("Failed to receive notification");
        }
    }

    private void connectToGateway() {
        CoapClient listener = null;
        try {
            this.client = new CoapClient("coap://localhost:5683/sensors");
            this.client.post(mapper.writeValueAsString(this.sensor), MediaTypeRegistry.TEXT_PLAIN);
            listener = new CoapClient("coap://localhost:5683/control");
            listener.observe(new ClientListener(this));
            while (this.sensor.isAlive()) {
                Thread.sleep(1);
            }
            System.out.println("Sensor id: " + this.sensor.getId() + " stops listening to gateway");
            // Thread.sleep(7 * 24 * 3600 * 1000);
        } catch (ConnectorException | IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (this.client != null) {
                this.client.shutdown();
            }
            if (listener != null) {
                listener.shutdown();
            }
        }
    }

    private void sendData() {
        CoapClient client = null;
        try {
            // System.out.format("Start send data after %d seconds\n", 1);
            client = new CoapClient("coap://localhost:5683/data-" + this.sensor.getId());
            this.sensor.startGenerateData(this.timeInterval, this.delay);
            if (this.timeInterval > 0) {
                long endTime = System.currentTimeMillis() + (this.timeInterval + 3000);
                while (System.currentTimeMillis() < endTime && this.sensor.isAlive()) {
                    if (this.sensor.isUpdated()) {
                        final DataMessage dataMessage = messageMapper.apply(sensor);
                        client.put(mapper.writeValueAsString(dataMessage), MediaTypeRegistry.TEXT_PLAIN);
                        this.sensor.setUpdated(false);
                    }
                }
            } else if (this.timeInterval == Sensor.DEFAULT_TIME_INTERVAL) {
                long startTime = System.currentTimeMillis();
                while (this.sensor.isAlive()) {
                    if (this.sensor.isUpdated()) {
                        final DataMessage dataMessage = messageMapper.apply(sensor);
                        client.put(mapper.writeValueAsString(dataMessage), MediaTypeRegistry.TEXT_PLAIN);
                        this.sensor.setUpdated(false);
                        startTime = System.currentTimeMillis();
                    }
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime > (this.delay + 60000) && !this.sensor.isUpdated()) {
                        break;
                    }
                }
            }
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (client != null) {
                client.shutdown();
            }
        }
        System.out.println("Sensor id: " + this.sensor.getId() + " stops sending data to gateway");
    }

    public void createConnection() {
        this.listenThread.start();
    }

    public void startSendData() {
        this.sendDataThread.start();
    }

    public void startSendData(long timeInterval) {
        this.timeInterval = timeInterval;
        this.sendDataThread.start();
    }

    public void startSendData(long timeInterval, long delay) {
        this.timeInterval = timeInterval;
        this.delay = delay;
        this.sendDataThread.start();
    }

    public void stop() {
        this.sensor.setAlive(false);
    }

    public static void main(String[] args) {
        Sensor sensor = new Sensor("sensor-1", DataGenerator.humidityGenerate());
        Sensor sensor1 = new Sensor("sensor-2", DataGenerator.humidityGenerate());
        Sensor sensor2 = new Sensor("sensor-3", DataGenerator.humidityGenerate());
        Sensor sensor3 = new Sensor("sensor-4", DataGenerator.humidityGenerate());
        Client client = new Client(sensor);
        Client client1 = new Client(sensor1);
        Client client2 = new Client(sensor2);
        Client client3 = new Client(sensor3);
        client.createConnection();
        client1.createConnection();
        client2.createConnection();
        client3.createConnection();
//        client.startSendData(3600 * 1000, 500);
//        client1.startSendData(3600 * 1000);
    }
}