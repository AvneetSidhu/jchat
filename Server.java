import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static Set<ClientHandler> clientHandlers = ConcurrentHashMap.newKeySet();
    private static final int PORT = 3000;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.printf("Server running on port %s%n", PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastMessage (String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient (ClientHandler client) {
        clientHandlers.remove(client);
    }
};

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in; 
    private String clientName; 

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            clientName = in.readLine();
            System.out.println(clientName + " has joined the chat");
            Server.broadcastMessage(clientName + " has joined the chat!", this);
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(clientName + ": " + message);
                Server.broadcastMessage(clientName + ": " + message, this);
            }

        } catch (IOException e) {
            System.out.println(clientName = " has disconnected");
        } finally {
            Server.removeClient(this);
            Server.broadcastMessage(clientName + " has left the chat", this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}