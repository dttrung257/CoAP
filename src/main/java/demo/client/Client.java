package demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.message.ControlMessage;
import demo.message.DataMessage;
import demo.utils.MessageMapper;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Date;

public class Client {
    private final Sensor sensor;
    private final Thread listenThread;
    private Thread sendDataThread;
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

        @Override
        public void onLoad(CoapResponse response) {
            if (response.getPayload().length > 0 && response.getPayload() != null) {
                try {
                    ControlMessage controlMessage = mapper.readValue(response.getPayload(), ControlMessage.class);
                    if (controlMessage.getSensorId().equalsIgnoreCase("ALL")
                            || Integer.parseInt(controlMessage.getSensorId()) == this.client.sensor.getId()) {
                        String message = controlMessage.getMessage();
                        switch (message) {
                            case ControlMessage.START, ControlMessage.RESUME -> start();
                            case ControlMessage.STOP -> stop();
                            default -> {
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
        try {
            this.client = new CoapClient("coap://localhost:5683/sensors");
            CoapResponse response = this.client.post(mapper.writeValueAsString(this.sensor), MediaTypeRegistry.TEXT_PLAIN);
            System.out.println(response.getResponseText());
            CoapClient listener = new CoapClient("coap://localhost:5683/control");
            listener.observe(new ClientListener(this));
            Thread.sleep(7 * 24 * 3600 * 1000);
        } catch (ConnectorException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendData() {
        try {
            // System.out.format("Start send data after %d seconds\n", 1);
            this.sensor.startGenerateData(this.timeInterval, this.delay);
            if (this.timeInterval > 0) {
                long endTime = System.currentTimeMillis() + (this.timeInterval + 3000);
                while (System.currentTimeMillis() < endTime) {
                    if (this.sensor.isUpdated()) {
                        final DataMessage dataMessage = messageMapper.apply(sensor);
                        this.client.put(mapper.writeValueAsString(dataMessage), MediaTypeRegistry.TEXT_PLAIN);
                        this.sensor.setUpdated(false);
                    }
                }
            } else if (this.timeInterval == Sensor.DEFAULT_TIME_INTERVAL) {
                long startTime = System.currentTimeMillis();
                while (true) {
                    if (this.sensor.isUpdated()) {
                        final DataMessage dataMessage = messageMapper.apply(sensor);
                        this.client.put(mapper.writeValueAsString(dataMessage), MediaTypeRegistry.TEXT_PLAIN);
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
        }
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

    public static void main(String[] args) {
        Sensor sensor = new Sensor(0.5, new Date(System.currentTimeMillis()));
        Sensor sensor1 = new Sensor(0.3, new Date(System.currentTimeMillis()));
        Client client = new Client(sensor);
        Client client1 = new Client(sensor1);
        client.createConnection();
        client1.createConnection();
//        client.startSendData(3600 * 1000, 500);
//        client1.startSendData(3600 * 1000);
    }
}
