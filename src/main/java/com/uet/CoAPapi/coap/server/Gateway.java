package com.uet.CoAPapi.coap.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.message.DataMessage;
import com.uet.CoAPapi.config.CoapConfig;
import com.uet.CoAPapi.utils.TimeUtil;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class Gateway extends CoapServer {
    private static final int COAP_PORT = 5683;
    private static final long LIMIT = 100;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Long, DataResource> dataResources = new HashMap<>();
    private static final String NON_NEGATIVE_INTEGER_REGEX = "^[+]?([1-9]\\d*|0)$";

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
            byte[] payload = exchange.getRequestPayload();
            if (payload.length > 0) {
                try {
                    Sensor sensor = mapper.readValue(payload, Sensor.class);
                    sensors.put(sensor.getId(), sensor);
                    DataResource dataResource = new DataResource("data-" + sensor.getId());
                    dataResources.put(sensor.getId(), dataResource);
                    getParent().add(dataResource);
                    System.out.println("Sensor id: " + sensor.getId() + " connected to gateway");
                    exchange.respond(CoAP.ResponseCode.CREATED);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

//        @Override
//        public void handlePUT(CoapExchange exchange) {
//            exchange.accept();
//            byte[] payload = exchange.getRequestPayload();
//            if (payload.length > 0) {
//                try {
//                    DataMessage message = mapper.readValue(payload, DataMessage.class);
//                    message.setLatency(System.currentTimeMillis() - message.getTimestamp());
//                    getAttributes().addAttribute("sensor-data");
//                    getAttributes().setAttribute("sensor-data", mapper.writeValueAsString(message));
//                    exchange.respond(CoAP.ResponseCode.CHANGED);
//                    changed();
//                    getAttributes().clearAttribute("sensor-data");
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }
    }

    public static class DataResource extends CoapResource {
        public DataResource(String name) {
            super(name);
            this.setObservable(true);
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            exchange.respond(CoAP.ResponseCode.CONTENT, getAttributes().getFirstAttributeValue("sensor-data"));
        }

        @Override
        public void handlePUT(CoapExchange exchange) {
            exchange.accept();
            byte[] payload = exchange.getRequestPayload();
            if (payload.length > 0) {
                try {
                    DataMessage message = mapper.readValue(payload, DataMessage.class);
                    message.setLatency(System.currentTimeMillis() - message.getTimestamp());
                    getAttributes().addAttribute("sensor-data");
                    getAttributes().setAttribute("sensor-data", mapper.writeValueAsString(message));
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
                try {
                    ControlMessage controlMessage = mapper.readValue(payload, ControlMessage.class);
                    getAttributes().addAttribute("data");
                    getAttributes().setAttribute("data", new String(payload, StandardCharsets.UTF_8));
                    if (controlMessage.getMessage().equalsIgnoreCase(ControlMessage.TERMINATE_MESSAGE)) {
                        if (controlMessage.getSensorId().matches(NON_NEGATIVE_INTEGER_REGEX)) {
                            DataResource dataResource = dataResources.get(Long.parseLong(controlMessage.getSensorId()));
                            getParent().delete(dataResource);
                            System.out.println("Remove data resource of sensor id: " + controlMessage.getSensorId());
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

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
