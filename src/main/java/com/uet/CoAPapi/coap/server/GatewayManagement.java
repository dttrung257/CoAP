package com.uet.CoAPapi.coap.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.message.DataMessage;
import com.uet.CoAPapi.dtos.DataResponse;
import com.uet.CoAPapi.utils.TimeUtil;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Scanner;

public class GatewayManagement {
    private static final String NON_NEGATIVE_INTEGER_REGEX = "^[+]?([1-9]\\d*|0)$";
    private static final ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
        CoapClient manager = new CoapClient("coap://localhost:5683/control");
        CoapClient client = new CoapClient("coap://localhost:5683/sensors");
        CoapHandler coapHandler = new CoapHandler() {
            @Override
            public void onLoad(CoapResponse coapResponse) {
                if (coapResponse.getPayload().length > 0) {
                    try {
                        DataMessage dataMessage = mapper.readValue(coapResponse.getPayload(), DataMessage.class);
                        DataResponse response = DataResponse.builder()
                                .name(dataMessage.getName())
                                .humidity(dataMessage.getHumidity())
                                .timestamp(TimeUtil.format(dataMessage.getTimestamp()))
                                .latency(dataMessage.getLatency())
                                .build();
                        System.out.println(response);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
            }

            @Override
            public void onError() {

            }
        };
        client.observe(coapHandler);
        Scanner scanner = new Scanner(System.in);
        String command;

        try {
            while (true) {
                System.out.println("Enter sensor id or ALL (QUIT to exit): ");
                command = scanner.nextLine();
                String id;
                int option;
                if (command.equalsIgnoreCase("QUIT")) {
                    break;
                } else if (command.equalsIgnoreCase("ALL")) {
                    id = "ALL";
                } else if (command.matches(NON_NEGATIVE_INTEGER_REGEX)) {
                    id = command;
                } else {
                    System.out.println("Unknown command");
                    continue;
                }
                System.out.println("Enter option (0: ON, 1: OFF, 2: TERMINATE) (QUIT to exit): ");
                command = scanner.nextLine();
                if (command.equalsIgnoreCase("QUIT")) {
                    break;
                } else if (command.matches(NON_NEGATIVE_INTEGER_REGEX)
                        && Integer.parseInt(command) < 3) {
                    option = Integer.parseInt(command);
                } else {
                    System.out.println("Unknown command");
                    continue;
                }
                ControlMessage controlMessage = new ControlMessage(id, option);
                manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            }
        } catch (IOException | ConnectorException e) {
            e.printStackTrace();
        }
    }
}
