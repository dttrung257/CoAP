package demo.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.message.ControlMessage;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.elements.exception.ConnectorException;

import java.io.IOException;
import java.util.Scanner;

public class GatewayManagement {
    private static final String NON_NEGATIVE_INTEGER_REGEX = "^[+]?([1-9]\\d*|0)$";
    private static final ObjectMapper mapper = new ObjectMapper();
    public static void main(String[] args) {
        CoapClient manager = new CoapClient("coap://localhost:5683/control");
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
                System.out.println("Enter option (0: START, 1: STOP, 2: RESUME) (QUIT to exit): ");
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
