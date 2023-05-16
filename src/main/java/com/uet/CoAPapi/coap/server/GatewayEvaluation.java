package com.uet.CoAPapi.coap.server;

import java.lang.management.ManagementFactory;

import com.sun.management.*;
import com.uet.CoAPapi.coap.client.Client;
import com.uet.CoAPapi.coap.client.DataGenerator;
import com.uet.CoAPapi.coap.client.Sensor;

public class GatewayEvaluation {
    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        gateway.start();
        System.out.println("Gateway started.");

        OperatingSystemMXBean bean = (com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean();
        double totalMem = bean.getTotalMemorySize();
        long maxNode = 0;
        while (true) {
            Sensor sensor = new Sensor(DataGenerator.humidityGenerate());
            Client client = new Client(sensor);
            client.createConnection();
            client.startSendData();
            maxNode++;
            System.out.println("Number of nodes: " + maxNode);
            double freeMemoryPercent = bean.getFreeMemorySize()/totalMem;
            double usedCPUProcess = bean.getProcessCpuLoad();
            double usedCPU = bean.getCpuLoad();
            System.out.println("Used CPU Process: " + usedCPUProcess * 100);
            System.out.println("Used CPU: " + usedCPU * 100);
            System.out.println("Used Memory: " + (1 - freeMemoryPercent) * 100 + " %");

            // RAM đo chính xác nhưng k tăng :(
            if (freeMemoryPercent < 0.25) {
                System.out.println("Reach memory capacity!");
                break;
            }

            // Thấy trên mức này thì CPU của máy ~ 90 %
            // used CPU Process giá trị gần bằng used CPU
            if (usedCPU > 0.30) {
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

        System.out.println("Max node: " + maxNode);

    }
}
