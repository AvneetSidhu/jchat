import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in; 
    private String name;
    public Client (String serverAddress, int PORT) {
        try {
            socket = new Socket(serverAddress, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        } catch (IOException e) {
            System.err.println("Unable to connect to the server");
            System.exit(1);
        }
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter display name:");
            name = scanner.nextLine();
            out.println(name);

            new Thread(new ServerListener()).start();

            String message;

            while (true) {
                System.out.print(name + ": ");
                message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("You have exited the chat");
                    break;
                }
                out.println(message);
            }
        } finally {
            closeResources();
        }
    }

    private void closeResources() {
        try {
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();

        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("Disconnected from the server.");
            }
        }
    }
    
    public static void main(String[] args) {
        String serverAddress = "localhost"; 
        int port = 3000;                   

        Client client = new Client(serverAddress, port);
        client.start();
    }
}
