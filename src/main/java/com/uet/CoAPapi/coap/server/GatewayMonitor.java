package com.uet.CoAPapi.coap.server;

import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.DataGenerator;
import com.uet.CoAPapi.coap.client.Sensor;
import com.uet.CoAPapi.config.CoapConfig;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.util.ArrayList;
import java.util.List;

public class GatewayMonitor {
    private static long maxNode = 0;
    public static List<Client> clients = new ArrayList<>();

    public static double getUsageCpu() {
        SystemInfo si = new SystemInfo();
        CentralProcessor cpu = si.getHardware().getProcessor();
        return cpu.getSystemCpuLoad(CoapConfig.sensors.get(0).getDelay()) * 100;
    }

    public static double getUsageRam() {
        SystemInfo si = new SystemInfo();
        GlobalMemory memory = si.getHardware().getMemory();
        return (memory.getTotal() * 1.0 - memory.getAvailable()) / (memory.getTotal()) * 100;
    }

    public static long evaluateMaxNode() {
        SystemInfo si = new SystemInfo();
        CentralProcessor cpu = si.getHardware().getProcessor();
        GlobalMemory memory = si.getHardware().getMemory();

        while (true) {
            double cpuUsage = cpu.getSystemCpuLoad(1000) * 100;
            double ramUsage = (memory.getTotal() * 1.0 - memory.getAvailable()) / (memory.getTotal()) * 100;
            Sensor sensor = new Sensor(DataGenerator.humidityGenerate());
            Client client = new Client(sensor);
            client.createConnection();
            client.startSendData();
            clients.add(client);
            maxNode++;
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
        return maxNode;
    }

    public static void main(String[] args) {
        long maxNode = evaluateMaxNode();
        System.out.println("Max node: " + maxNode);
    }
}