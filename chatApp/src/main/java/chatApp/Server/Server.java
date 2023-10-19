package chatApp.Server;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;

import chatApp.Server.UserManager.AuthResult;

public class Server {
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static boolean isRunning = true; 
    private static ServerSocket listener;
    private static UserManager userManager;

    static {
        try {
            userManager = new UserManager();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1); // Exit if there's a problem initializing UserManager
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Chat server started...");

        userManager.dropUserTable();
        userManager.createUserTable();

        listener = new ServerSocket(59001);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down the server...");
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        while (isRunning) {
            new Handler(listener.accept()).start();
        }
    }

    private static class Handler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private String clientName;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                out = new PrintWriter(output, true);

                String initialMessage = reader.readLine();

                if (initialMessage.startsWith("AUTHENTICATE:")) {
                    String[] parts = initialMessage.split(":");
                    String username = parts[1];
                    String password = parts[2];
        
                    AuthResult result = authenticate(username, password);
        
                    switch(result) {
                        case SUCCESS:
                            out.println("AUTH_SUCCESS");
                            clientName = username;
                            clientWriters.add(out);
                            break;
                        case WRONG_USERNAME:
                            out.println("AUTH_FAILED: Incorrect username.");
                            return;
                        case WRONG_PASSWORD:
                            out.println("AUTH_FAILED: Incorrect password.");
                            return;
                        case ERROR:
                        default:
                            out.println("AUTH_FAILED: An error occurred.");
                            return;
                    }
                } else if (initialMessage.startsWith("SIGNUP:")) {
                    String[] parts = initialMessage.split(":");
                    String username = parts[1];
                    String password = parts[2];

                    if (signup(username, password)) {
                        out.println("SIGNUP_SUCCESS");
                    } else {
                        out.println("SIGNUP_FAILED");
                        return;
                    }
                }

                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        return;
                    }

                    System.out.println("Received from " + clientName + ": " + message);
                    String formattedMessage = clientName + ": " + message;
                    for (PrintWriter writer : clientWriters) {
                        writer.println(formattedMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                clientWriters.remove(out);
            }
        }
    }

    private static AuthResult authenticate(String username, String password) {
        return userManager.authenticateUser(username, password);
    }

    private static boolean signup(String username, String password) {
        try {
            userManager.createUser(username, password);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void stopServer() {
        isRunning = false;
        try {
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
