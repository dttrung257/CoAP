package demo.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Client {
    private Sensor sensor;
    private CoapClient client;
    private Thread th;
    private static final ObjectMapper mapper = new ObjectMapper();
    public Client(final Sensor sensor) {
        this.sensor = sensor;
        this.th = new Thread(() -> {
            try {
                this.getConnect();
            } catch (IOException | ConnectorException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public CoapClient getClient() {
        return client;
    }

    public void setClient(CoapClient client) {
        this.client = client;
    }

    private static class NodeObserveHandler implements CoapHandler {
        @Override
        public void onLoad(CoapResponse coapResponse) {

        }

        @Override
        public void onError() {

        }
    }

    private void getConnect() throws IOException, ConnectorException, URISyntaxException {
        URI uri = new URI("coap://localhost:5683/connect");
        this.client = new CoapClient(uri);
        this.client.post(mapper.writeValueAsString(this.sensor), MediaTypeRegistry.TEXT_PLAIN);
        this.client.observe(new NodeObserveHandler());
    }

    public void start() {
        this.th.start();
    }

    public static void main(String[] args) {
        Sensor sensor = new Sensor(0.5, 3000);
        Sensor sensor1 = new Sensor(0.3, 5000);
        Client client = new Client(sensor);
        Client client1 = new Client(sensor1);
        client.start();
        client1.start();
    }
}
