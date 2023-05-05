package com.uet.CoAPapi.coap.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.message.DataMessage;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
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
            exchange.respond(CoAP.ResponseCode.CONTENT, getAttributes().getFirstAttributeValue("sensor-data"));
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            exchange.accept();
            byte[] sensorInfo = exchange.getRequestPayload();
            if (sensorInfo.length > 0) {
                try {
                    Sensor sensor = mapper.readValue(sensorInfo, Sensor.class);
                    // System.out.println(sensor);
                    sensors.put(sensor.getId(), sensor);
                    exchange.respond(CoAP.ResponseCode.CREATED, "Connection is created");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void handlePUT(CoapExchange exchange) {
            exchange.accept();
            byte[] payload = exchange.getRequestPayload();
            if (payload.length > 0) {
                try {
                    DataMessage message = mapper.readValue(payload, DataMessage.class);
                    getAttributes().addAttribute("sensor-data");
                    getAttributes().setAttribute("sensor-data", new String(payload, StandardCharsets.UTF_8));
//                System.out.println("id: " + message.getId() +
//                        ", humidity: " + message.getHumidity() +
//                        ", time: " + message.getTimestamp());
                    // dataQueue.offer(message);
                    exchange.respond(CoAP.ResponseCode.CHANGED);
                    changed();
                    getAttributes().clearAttribute("sensor-data");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
            if (payload.length > 0) {
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
        (new CoapClient("coap://localhost:5683/sensors")).observe(new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                if (coapResponse.getPayload().length > 0) {
                    try {
                        DataMessage dataMessage = mapper.readValue(coapResponse.getPayload(), DataMessage.class);
                        System.out.println(dataMessage);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void onError() {
                System.err.println("Failed to receive notification");
            }
        });
    }
}
