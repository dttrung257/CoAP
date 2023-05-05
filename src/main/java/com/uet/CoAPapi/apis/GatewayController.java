package com.uet.CoAPapi.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.config.CoapConfig;
import com.uet.CoAPapi.dtos.NewSensor;
import com.uet.CoAPapi.dtos.SensorDelay;
import com.uet.CoAPapi.dtos.SensorDto;
import com.uet.CoAPapi.dtos.SensorState;
import com.uet.CoAPapi.exception.SensorAlreadyExistsException;
import com.uet.CoAPapi.exception.UnknownSensorStateException;
import com.uet.CoAPapi.mappers.SensorDtoMapper;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.message.DataMessage;
import com.uet.CoAPapi.repositories.SensorRepo;
import jakarta.validation.Valid;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gateway")
@Validated
public class GatewayController {
    private final SensorRepo sensorRepo;
    private final SensorDtoMapper sensorDtoMapper;
    private final CoapClient coapClient;
    private final CoapClient manager;
    private static final ObjectMapper mapper = new ObjectMapper();

    public GatewayController(SensorRepo sensorRepo, SensorDtoMapper sensorDtoMapper) {
        this.sensorRepo = sensorRepo;
        this.sensorDtoMapper = sensorDtoMapper;
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
    @PutMapping("/sensors/state")
    public ResponseEntity<String> changeSensorState(@RequestBody @Valid SensorState sensorState) {
        if (sensorState.getState().equalsIgnoreCase(ControlMessage.ON)) {
            ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_ON);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok("Success turn on all sensors");
        } else if (sensorState.getState().equalsIgnoreCase(ControlMessage.OFF)) {
            ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_OFF);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok("Success turn off all sensors");
        } else {
            throw new UnknownSensorStateException("Unknown sensor state: " + sensorState.getState());
        }
    }

    // Change speed
    @PutMapping("/sensors/speed")
    public ResponseEntity<String> changeSpeed(@RequestBody @Valid SensorDelay sensorDelay) {
        ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_ON, (long) (sensorDelay.getDelay() * 1000));
        try {
            this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Change speed successfully");
    }


    // Turn on sensors by id
    // Turn off sensors by id
    @PutMapping("/sensors/{id}/state")
    public ResponseEntity<String> changeSensorStateById(@PathVariable(value = "id", required = true) Long id,
                                               @RequestBody @Valid SensorState sensorState) {
        if (sensorState.getState().equalsIgnoreCase(ControlMessage.ON)) {
            ControlMessage controlMessage = new ControlMessage(id.toString(), ControlMessage.TURN_ON);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok("Success turn on sensor id: " + id);
        } else if (sensorState.getState().equalsIgnoreCase(ControlMessage.OFF)) {
            ControlMessage controlMessage = new ControlMessage(id.toString(), ControlMessage.TURN_OFF);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok("Success turn off sensor id: " + id);
        } else {
            throw new UnknownSensorStateException("Unknown sensor state: " + sensorState.getState());
        }
    }


    // Get sensors
    @GetMapping("/sensors")
    public ResponseEntity<List<SensorDto>> getSensors() {
        final List<SensorDto> sensorDtos = CoapConfig.sensors.stream().map(sensorDtoMapper).toList();
        return ResponseEntity.ok(sensorDtos);
    }

    // Get sensors by id
    @GetMapping("sensors/{id}")
    public ResponseEntity<SensorDto> getSensorById(@PathVariable(value = "id", required = true) Long id) {
        final SensorDto sensorDto = CoapConfig.sensors.stream()
                .filter(s -> s.getId() == id).map(sensorDtoMapper)
                .toList().get(0);
        return ResponseEntity.ok(sensorDto);
    }


    // Create sensor
    @PostMapping("/sensors")
    public ResponseEntity<SensorDto> createSensor(@RequestBody @Valid NewSensor newSensor) {
        if (sensorRepo.existsByName(newSensor.getName())) {
            throw new SensorAlreadyExistsException("Sensor name: " + newSensor.getName() + " already exists");
        }
        final Sensor sensor = new Sensor();
        sensor.setName(newSensor.getName());
        sensor.loadInitData();
        Client client;
        if (!CoapConfig.sensors.isEmpty()) {
            sensor.setDelay(CoapConfig.sensors.get(0).getDelay());
            client = new Client(sensor);
            client.setDelay(CoapConfig.sensors.get(0).getDelay());
        } else {
            sensor.setDelay(Sensor.DEFAULT_DELAY);
            client = new Client(sensor);
            client.setDelay(Sensor.DEFAULT_DELAY);
        }
        client.createConnection();
        CoapConfig.sensors.add(sensor);
        sensorRepo.save(sensor);
        ControlMessage controlMessage = new ControlMessage(Long.toString(sensor.getId()), ControlMessage.TURN_ON);
        try {
            this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        }
        return new ResponseEntity<>(sensorDtoMapper.apply(sensor), HttpStatus.CREATED);
    }
}

