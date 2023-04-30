package demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.message.DataMessage;
import demo.utils.MessageMapper;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.observe.ObserveRelation;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Date;

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

    private static class ClientListener implements CoapHandler {
        @Override
        public void onLoad(CoapResponse response) {
            System.out.println(response.getResponseText());
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
            listener.observe(new ClientListener());
            Thread.sleep(7 * 24 * 3600 * 1000);
//            while (true) {
//
//                Thread.sleep(7 * 24 * 3600 * 1000);
//            }
        } catch (ConnectorException | IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendData() {
        try {
            System.out.format("Start send data after %d seconds\n", 1);
            Thread.sleep(1000);
            this.sensor.startGenerateData(this.timeInterval, this.delay);
            long endTime = System.currentTimeMillis() + (this.timeInterval + 3000);
            while (System.currentTimeMillis() < endTime) {
                if (this.sensor.isUpdated()) {
                    final DataMessage dataMessage = messageMapper.apply(sensor);
                    this.client.put(mapper.writeValueAsString(dataMessage), MediaTypeRegistry.TEXT_PLAIN);
                    this.sensor.setUpdated(false);
                }
            }
            this.sendDataThread.interrupt();
        } catch (ConnectorException | IOException | InterruptedException e) {
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
        client.startSendData(7000, 1000);
        client1.startSendData(10000);
    }
}
