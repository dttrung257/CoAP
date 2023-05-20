package com.uet.CoAPapi.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.coap.server.Gateway;
import com.uet.CoAPapi.coap.server.GatewayMonitor;
import com.uet.CoAPapi.config.CoapConfig;
import com.uet.CoAPapi.dtos.*;
import com.uet.CoAPapi.exception.*;
import com.uet.CoAPapi.mappers.ControlMessageDtoMapper;
import com.uet.CoAPapi.mappers.SensorDtoMapper;
import com.uet.CoAPapi.coap.message.ControlMessage;
import com.uet.CoAPapi.coap.message.DataMessage;
import com.uet.CoAPapi.repositories.SensorRepo;
import com.uet.CoAPapi.utils.TimeUtil;
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
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/gateway")
@Validated
public class GatewayController {
    private final SensorRepo sensorRepo;
    private final SensorDtoMapper sensorDtoMapper;
    private final ControlMessageDtoMapper controlMessageDtoMapper;
    private final CoapClient manager;
    private final Gateway gateway;
    private static final ObjectMapper mapper = new ObjectMapper();


    public GatewayController(SensorRepo sensorRepo,
                             SensorDtoMapper sensorDtoMapper,
                             ControlMessageDtoMapper controlMessageDtoMapper,
                             Gateway gateway) {
        this.sensorRepo = sensorRepo;
        this.sensorDtoMapper = sensorDtoMapper;
        this.controlMessageDtoMapper = controlMessageDtoMapper;
        this.gateway = gateway;
        this.manager = new CoapClient("coap://localhost:5683/control");
    }

//    @GetMapping(value = "/data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<DataResponse> getDataMessages() {
//        ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_ON_OPTION);
//        try {
//            manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
//        } catch (ConnectorException | IOException e) {
//            throw new RuntimeException(e);
//        }
//        return Flux.create(emitter -> {
//            CoapClient client = new CoapClient("coap://localhost:5683/sensors");
//            CoapHandler coapHandler = new CoapHandler() {
//                @Override
//                public void onLoad(CoapResponse coapResponse) {
//                    if (coapResponse.getPayload().length > 0) {
//                        try {
//                            DataMessage dataMessage = mapper.readValue(coapResponse.getPayload(), DataMessage.class);
//                            DataResponse response = DataResponse.builder()
//                                    .id(dataMessage.getId())
//                                    .name(dataMessage.getName())
//                                    .humidity(dataMessage.getHumidity())
//                                    .timestamp(TimeUtil.format(dataMessage.getTimestamp()))
//                                    .latency(dataMessage.getLatency())
//                                    .build();
//                            emitter.next(response);
//                        } catch (IOException e) {
//                            emitter.error(e);
//                        }
//                    }
//                }
//
//                @Override
//                public void onError() {
//                    emitter.error(new RuntimeException("Failed to receive notification"));
//                }
//            };
//            client.observe(coapHandler);
//        });
//    }
//
//    @GetMapping(value = "/test", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<DataResponse> test() {
//        ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_ON_OPTION);
//        try {
//            manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
//        } catch (ConnectorException | IOException e) {
//            throw new RuntimeException(e);
//        }
//        CoapClient client = new CoapClient("coap://localhost:5683/sensors");
//        CoapHandler coapHandler = new CoapHandler() {
//            @Override
//            public void onLoad(CoapResponse coapResponse) {
//                if (coapResponse.getPayload().length > 0) {
//                    try {
//                        DataMessage dataMessage = mapper.readValue(coapResponse.getPayload(), DataMessage.class);
//                        DataResponse response = DataResponse.builder()
//                                .id(dataMessage.getId())
//                                .name(dataMessage.getName())
//                                .humidity(dataMessage.getHumidity())
//                                .timestamp(TimeUtil.format(dataMessage.getTimestamp()))
//                                .latency(dataMessage.getLatency())
//                                .build();
//                        System.out.println(response);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onError() {
//                System.out.println("Failed to receive notification");
//            }
//        };
//        client.observe(coapHandler);
//        return null;
//    }

    @GetMapping(value = "/{id}/data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DataResponse> getDataMessagesById(@PathVariable("id") Long id) {
        ControlMessage controlMessage = new ControlMessage(id.toString(), ControlMessage.TURN_ON_OPTION);
        try {
            manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        }
        return Flux.create(emitter -> {
            CoapClient client = new CoapClient("coap://localhost:5683/data-" + id);
            CoapHandler coapHandler = new CoapHandler() {
                @Override
                public void onLoad(CoapResponse coapResponse) {
                    if (coapResponse.getPayload().length > 0) {
                        try {
                            DataMessage dataMessage = mapper.readValue(coapResponse.getPayload(), DataMessage.class);
                            if (dataMessage.getId() == id) {
                                DataResponse response = DataResponse.builder()
                                        .id(dataMessage.getId())
                                        .name(dataMessage.getName())
                                        .humidity(dataMessage.getHumidity())
                                        .timestamp(TimeUtil.format(dataMessage.getTimestamp()))
                                        .latency(dataMessage.getLatency())
                                        .throughput(dataMessage.getThroughput())
                                        .build();
                                System.out.println(response);
                                CoapConfig.sensors.stream().filter(s -> s.getId() == id).toList().get(0)
                                        .setThroughput(dataMessage.getThroughput());
                                emitter.next(response);
                            }
                        } catch (IOException e) {
                            emitter.error(e);
                        }
                    }
                }

                @Override
                public void onError() {
                    emitter.error(new RuntimeException("Failed to receive notification"));
                }
            };
            client.observe(coapHandler);
        });
    }

    // Get performance
    @GetMapping(value = "/performance", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Performance> getPerformanceData() {
        return Flux.create(emitter -> {
            Scheduler.Worker worker = Schedulers.newSingle("performance-worker").createWorker();
            worker.schedulePeriodically(() -> {
                double totalThroughput = CoapConfig.sensors.stream().mapToDouble(Sensor::getThroughput).sum();
                final Performance performance = Performance.builder()
                        .usageCpu(GatewayMonitor.getUsageCpu())
                        .usageRam(GatewayMonitor.getUsageRam())
                        .timestamp(TimeUtil.format(System.currentTimeMillis()))
                        .throughput(totalThroughput)
                        .build();
                emitter.next(performance);
            }, 0, CoapConfig.sensors.get(0).getDelay() / 1000, TimeUnit.SECONDS);

            // Đảm bảo hủy tác vụ khi subscriber không còn kết nối
            emitter.onDispose(worker);
        });
    }

    // Get control messages
    @GetMapping("/control-messages")
    public ResponseEntity<List<ControlMessageDto>> getControlMessages() {
        return ResponseEntity.ok(Gateway.controlMessages.stream().map(controlMessageDtoMapper).toList());
    }

    // Get Max Node
    @GetMapping("/max-node")
    public ResponseEntity<Long> getMaxNode() {
        return ResponseEntity.ok(CoapConfig.maxNode);
    }

    // Turn off all sensors
    // Turn on all sensors
    @PutMapping("/sensors/state")
    public ResponseEntity<String> changeSensorState(@RequestBody @Valid SensorState sensorState) {
        if (sensorState.getState().equalsIgnoreCase(ControlMessage.TURN_ON_MESSAGE)) {
            ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_ON_OPTION);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.ok("Success turn on all sensors");
        } else if (sensorState.getState().equalsIgnoreCase(ControlMessage.TURN_OFF_MESSAGE)) {
            ControlMessage controlMessage = new ControlMessage("ALL", ControlMessage.TURN_OFF_OPTION);
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
        ControlMessage controlMessage = new ControlMessage("ALL", (long) (sensorDelay.getDelay() * 1000));
        try {
            this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            //CoapConfig.sensors.forEach(s -> s.setDelay((long) (sensorDelay.getDelay() * 1000)));
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Change speed successfully");
    }


    // Turn on sensors by id
    // Turn off sensors by id
    @PutMapping("/sensors/{id}/state")
    public ResponseEntity<SensorDto> changeSensorStateById(@PathVariable(value = "id", required = true) Long id,
                                               @RequestBody @Valid SensorState sensorState) {
        if (!sensorRepo.existsById(id) || CoapConfig.sensors.stream().noneMatch(s -> s.getId() == id)) {
            throw new SensorNotFoundException("Sensor id: " + id + " not found");
        }
        if (sensorState.getState().equalsIgnoreCase(ControlMessage.TURN_ON_MESSAGE)) {
            ControlMessage controlMessage = new ControlMessage(id.toString(), ControlMessage.TURN_ON_OPTION);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            CoapConfig.sensors.stream().filter(s -> s.getId() == id).toList().get(0).setRunning(true);
        } else if (sensorState.getState().equalsIgnoreCase(ControlMessage.TURN_OFF_MESSAGE)) {
            ControlMessage controlMessage = new ControlMessage(id.toString(), ControlMessage.TURN_OFF_OPTION);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            CoapConfig.sensors.stream().filter(s -> s.getId() == id).toList().get(0).setRunning(false);
        } else {
            throw new UnknownSensorStateException("Unknown sensor state: " + sensorState.getState());
        }
        final SensorDto sensorDto = CoapConfig.sensors.stream().filter(s -> s.getId() == id)
                .map(sensorDtoMapper).toList().get(0);
        return ResponseEntity.ok(sensorDto);
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
        if (CoapConfig.maxNode > CoapConfig.sensors.size()) {
            if (sensorRepo.existsByName(newSensor.getName()) ||
                    CoapConfig.sensors.stream().anyMatch(s -> s.getName().equalsIgnoreCase(newSensor.getName()))) {
                throw new SensorAlreadyExistsException("Sensor name: " + newSensor.getName() + " already exists");
            }

            Sensor sensor = new Sensor();
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
            sensor = sensorRepo.save(sensor);
            gateway.createResource("data-" + sensor.getId());
            System.out.println("Create data resource for sensor id: " + sensor.getId());
            client.createConnection();
            CoapConfig.sensors.add(sensor);
            ControlMessage controlMessage = new ControlMessage(Long.toString(sensor.getId()), ControlMessage.TURN_ON_OPTION);
            try {
                this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
            } catch (ConnectorException | IOException e) {
                throw new RuntimeException(e);
            }
            return new ResponseEntity<>(sensorDtoMapper.apply(sensor), HttpStatus.CREATED);
        } else {
            throw new ReachMaxNodeException("Reach max node");
        }
    }

    // Rename sensor
    @PutMapping("/sensors/{id}/name")
    public ResponseEntity<SensorDto> changeSensorName(@PathVariable(value = "id", required = true) Long id,
                                                        @RequestBody @Valid SensorName sensorName) {
        final Optional<Sensor> sensorOptional = sensorRepo.findById(id);
        if (CoapConfig.sensors.stream().anyMatch(s -> s.getId() == id) && sensorOptional.isPresent()
            && sensorRepo.findByNameWithOtherId(id, sensorName.getName()).isEmpty()) {
            CoapConfig.sensors.stream().filter(s -> s.getId() == id).toList().get(0).setName(sensorName.getName());
            final Sensor sensor = sensorOptional.get();
            sensor.setName(sensorName.getName());
            sensorRepo.save(sensor);
            return ResponseEntity.ok(sensorDtoMapper
                    .apply(CoapConfig.sensors.stream().filter(s -> s.getId() == id).toList().get(0)));
        } else if (sensorRepo.findByNameWithOtherId(id, sensorName.getName()).size() > 0) {
            throw new SensorAlreadyExistsException("Sensor name: " + sensorName.getName() + " already exists");
        } else {
            throw new SensorNotFoundException("Sensor id: " + id + " not found");
        }
    }

    // Delete sensor
    @DeleteMapping("/sensors/{id}")
    public ResponseEntity<String> deleteSensorById(@PathVariable(value = "id", required = true) Long id) {
        final Optional<Sensor> optionalSensor = sensorRepo.findById(id);
        if (optionalSensor.isEmpty() || CoapConfig.sensors.stream().noneMatch(s -> s.getId() == id)) {
            throw new SensorNotFoundException("Sensor id: " + id + " not found");
        }
        if (CoapConfig.sensors.stream().filter(s -> s.getId() == id).toList().get(0).isRunning()) {
            throw new CannotDeleteRunningSensorException("Sensor id: " + id + " cannot be deleted while it is on");
        }
        ControlMessage controlMessage = new ControlMessage(id.toString(), ControlMessage.TERMINATE_OPTION);
        try {
            this.manager.post(mapper.writeValueAsString(controlMessage), MediaTypeRegistry.TEXT_PLAIN);
        } catch (ConnectorException | IOException e) {
            throw new RuntimeException(e);
        }
        CoapConfig.sensors.removeIf(s -> s.getId() == id);
        sensorRepo.delete(optionalSensor.get());
        return ResponseEntity.ok("Delete sensor id: " + id + " successfully");
    }

}