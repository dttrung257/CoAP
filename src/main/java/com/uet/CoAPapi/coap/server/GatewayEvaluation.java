package com.uet.CoAPapi.coap.server;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import com.sun.management.*;
import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.DataGenerator;
import com.uet.CoAPapi.coap.client.Sensor;

public class GatewayEvaluation {
    private long maxNode;
    private static List<Client> clients;

    public GatewayEvaluation() {
        maxNode = 0;
        clients = new ArrayList<>();
    }

    public long evaluateMaxNode() {
        Gateway gateway = new Gateway();
        gateway.start();
        System.out.println("Gateway started.");

        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        double totalMem = bean.getTotalMemorySize();
        while (true) {
            Sensor sensor = new Sensor(DataGenerator.humidityGenerate());
            Client client = new Client(sensor);
            client.createConnection();
            client.startSendData();
            clients.add(client);
            this.maxNode++;
            System.out.println("Number of nodes: " + maxNode);
            double freeMemoryPercent = bean.getFreeMemorySize()/totalMem;
            double usedCPUProcess = bean.getProcessCpuLoad();
            double usedCPU = bean.getCpuLoad();
            System.out.println("Used CPU Process: " + usedCPUProcess * 100);
            System.out.println("Used CPU: " + usedCPU * 100);
            System.out.println("Used Memory: " + (1 - freeMemoryPercent) * 100 + " %");

            // RAM đo chính xác nhưng k tăng :(
            if (freeMemoryPercent < 0.1) {
                System.out.println("Reach memory capacity!");
                break;
            }

            // Thấy trên mức này thì CPU của máy ~ 90 %
            // used CPU Process giá trị gần bằng used CPU
            if (usedCPU > 0.9) {
                System.out.println("Reach CPU capacity!");
                break;
            }
            try {
                Thread.sleep(1000);
            }
            catch (Exception e){
                System.out.println(e.getMessage());
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
        GatewayEvaluation evaluation = new GatewayEvaluation();
        System.out.println("Max node: " + evaluation.evaluateMaxNode());
        Thread.interrupted();
    }
}
