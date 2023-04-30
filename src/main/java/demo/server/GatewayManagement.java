package demo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.client.Client;
import demo.client.Sensor;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class GatewayManagement {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
        Gateway gateway = new Gateway();
        gateway.start();
        System.out.format("Server is running on port %d\n",
                gateway.getEndpoints().get(0).getAddress().getPort());
        CoapClient manager = new CoapClient("coap://localhost:5683/control");
        Scanner scanner = new Scanner(System.in);
        String cmd;
        int id;

        try {
            do {
                System.out.print("Sensor ID: ");
                id = Integer.parseInt(scanner.nextLine());
                System.out.print("Enter Command: ");
                cmd = scanner.nextLine();

                if (!cmd.equals("QUIT")) {
                    String json = mapper.writeValueAsString(id + ", " + cmd);
                    CoapResponse response = manager.post(json, MediaTypeRegistry.TEXT_PLAIN);
                    System.out.println("Gateway: " + response.getResponseText());
                }
            } while (!cmd.equals("QUIT"));
        } catch (IOException | ConnectorException e) {
            e.printStackTrace();
        }
    }
}
