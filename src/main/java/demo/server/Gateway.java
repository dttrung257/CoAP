package demo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.client.Sensor;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Gateway extends CoapServer {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Long, Sensor> sensors = new HashMap<>();

    public Gateway() {
        this.add(new ConnectResource("connect"));
    }

    private static class ConnectResource extends CoapResource {

        public ConnectResource(String name) {
            super(name);
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            exchange.accept();
            byte[] sensorInfo = exchange.getRequestPayload();
            try {
                Sensor sensor = mapper.readValue(sensorInfo, Sensor.class);
                sensors.put(sensor.getId(), sensor);
                System.out.println("\nNumber sensor connect: " + sensors.size());
                System.out.println("\n\t========= Sensors ==========\n");
                for (Sensor s : sensors.values()) {
                    System.out.println(s);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        gateway.start();
        System.out.format("Server is running on port %d\n", gateway.getEndpoints().get(0).getAddress().getPort());
    }
}
