package demo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.client.Sensor;
import demo.message.ControlMessage;
import demo.message.DataMessage;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Gateway extends CoapServer {
    private static final int COAP_PORT = 5683;
    private static final long LIMIT = 100;
    private static final ObjectMapper mapper = new ObjectMapper();

    public Gateway() {
        this.add(new SensorResource("sensors"));
        this.add(new ControlResource("control"));
    }

    public static class SensorResource extends CoapResource {
        private static final Map<Long, Sensor> sensors = new HashMap<>();

        public SensorResource(String name) {
            super(name);
            this.setObservable(true);
        }

        @Override
        public void handleGET(CoapExchange exchange) {
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            exchange.accept();
            byte[] sensorInfo = exchange.getRequestPayload();
            try {
                Sensor sensor = mapper.readValue(sensorInfo, Sensor.class);
                // System.out.println(sensor);
                sensors.put(sensor.getId(), sensor);
                exchange.respond(CoAP.ResponseCode.CREATED, "Connection is created");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void handlePUT(CoapExchange exchange) {
            exchange.accept();
            byte[] data = exchange.getRequestPayload();
            try {
                DataMessage message = mapper.readValue(data, DataMessage.class);
                System.out.println("id: " + message.getId() +
                        ", humidity: " + message.getHumidity() +
                        ", time: " + message.getTimestamp());
                // dataQueue.offer(message);
                exchange.respond(CoAP.ResponseCode.CREATED);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ControlResource extends CoapResource {

        public ControlResource(String name) {
            super("control");
            setObservable(true);
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(CoAP.ResponseCode.CONTENT, getAttributes().getFirstAttributeValue("data"));
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            byte[] payload = exchange.getRequestPayload();
            if (payload != null) {
                getAttributes().addAttribute("data");
                getAttributes().setAttribute("data", new String(payload, StandardCharsets.UTF_8));
            }
            exchange.respond(CoAP.ResponseCode.CREATED, payload);
            changed();
            getAttributes().clearAttribute("data");
        }
    }

    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        gateway.start();
        System.out.format("Server is running on port %d\n",
                gateway.getEndpoints().get(0).getAddress().getPort());
    }
}
