package com.uet.CoAPapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.message.ControlMessage;
import com.uet.CoAPapi.message.DataMessage;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {
    private final CoapClient coapClient;
    private final CoapClient manager;
    private static final ObjectMapper mapper = new ObjectMapper();

    public GatewayController() {
        this.coapClient = new CoapClient("coap://localhost:5683/sensors");
        this.manager = new CoapClient("coap://localhost:5683/control");
    }

    @GetMapping(value = "/data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DataMessage> getDataMessages() {
        ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_ON);
        try {
            this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        }
        Flux<CoapResponse> coapFlux = Flux.create(emitter -> {
            CoapHandler handler = new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    emitter.next(coapResponse);
                }

                @Override
                public void onError() {
                    emitter.error(new RuntimeException("Failed to receive notification"));
                }
            };
            coapClient.observe(handler);
        });

        return coapFlux
                .filter(coapResponse -> coapResponse.getPayload().length > 0)
                .map(coapResponse -> {
                    try {
                        DataMessage dataMessage = mapper.readValue(coapResponse.getPayload(), DataMessage.class);
                        System.out.println(dataMessage);
                        return dataMessage;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    // Turn off all sensors

    // Turn on all sensors

    // Turn off sensors by id

    // Turn on sensors by id

    // Change speed
}

