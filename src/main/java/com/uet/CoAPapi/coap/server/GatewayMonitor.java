package com.uet.CoAPapi.coap.server;

import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.DataGenerator;
import com.uet.CoAPapi.coap.client.Sensor;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.util.ArrayList;
import java.util.List;

public class GatewayMonitor {
    private long maxNode;
    private static List<Client> clients;

    public GatewayMonitor() {
        maxNode = 0;
        clients = new ArrayList<>();
    }

    public long evaluateMaxNode() {
        SystemInfo si = new SystemInfo();
        CentralProcessor cpu = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();
        Gateway gateway = new Gateway();
        gateway.start();

        while (true) {
            double cpuUsage = cpu.getSystemCpuLoad(1000) * 100;
            double ramUsage = (memory.getTotal() * 1.0 - memory.getAvailable()) / (memory.getTotal()) * 100;
            Sensor sensor = new Sensor(DataGenerator.humidityGenerate());
            Client client = new Client(sensor);
            client.createConnection();
            client.startSendData();
            clients.add(client);
            this.maxNode++;
            System.out.println("Cpu Usage: " + cpuUsage + "%");
            System.out.println("Ram usage: " + ramUsage + "%");
            if (cpuUsage > 90) {
                System.out.println("Reach max cpu usage");
                break;
            }
            if (ramUsage > 90) {
                System.out.println("Reach max ram usage");
                break;
            }
        }
        for (Client client : clients) {
            client.stop();
        }
        gateway.stop();
        return this.maxNode;
    }

    public static void main(String[] args) {
        GatewayMonitor monitor = new GatewayMonitor();
        long maxNode = monitor.evaluateMaxNode();
        System.out.println("Max node: " + maxNode);
    }
}