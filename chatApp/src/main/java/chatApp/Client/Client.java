package chatApp.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private JFrame frame;
    private JTextArea messagesArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;

    private String clientName;
    private String password;

    public Client() {
        try {
            int choice = JOptionPane.showOptionDialog(null, "Choose an option", "Login/Signup", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"Login", "Signup"}, "Login");
            
            if(choice == 0) { // 0 for Login, 1 for Signup
                performLogin();
            } else {
                performSignup();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void performLogin() {
        clientName = JOptionPane.showInputDialog(null, "Enter your username: ");
        password = JOptionPane.showInputDialog(null, "Enter your password: ");
        
        if(clientName != null && !clientName.trim().isEmpty() && password != null) {
            frameSetup();
            startChatClient();
        } else {
            JOptionPane.showMessageDialog(null, "Username or Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void performSignup() {
        clientName = JOptionPane.showInputDialog(null, "Choose your username: ");
        password = JOptionPane.showInputDialog(null, "Choose your password: ");
        
        if(clientName != null && !clientName.trim().isEmpty() && password != null) {
            try {
                Socket socket = new Socket("localhost", 59001);
                PrintWriter signupOut = new PrintWriter(socket.getOutputStream(), true);
                signupOut.println("SIGNUP:" + clientName + ":" + password);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                
                if("SIGNUP_SUCCESS".equals(response)) {
                    JOptionPane.showMessageDialog(null, "Signup successful!");
                    frameSetup();
                    startChatClient();
                } else {
                    JOptionPane.showMessageDialog(null, "Signup failed! Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                }

            } catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Username or Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void frameSetup() {
        frame = new JFrame("Chat Client");

        messagesArea = new JTextArea(20, 30);
        messagesArea.setEditable(false);
        
        JScrollPane scrollPane = new JScrollPane(messagesArea);
        
        inputField = new JTextField(25);
        sendButton = new JButton("Send");

        sendButton.addActionListener(this::sendMessage);
        inputField.addActionListener(this::sendMessage);

        JPanel panel = new JPanel();
        panel.add(inputField);
        panel.add(sendButton);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void sendMessage(ActionEvent event) {
        String message = inputField.getText();
        if (message != null && !message.trim().isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void startChatClient() {
        try {
            Socket socket = new Socket("localhost", 59001);
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("AUTHENTICATE:" + clientName + ":" + password);
            String response = reader.readLine(); // Wait for a response from the server after authentication
            if (response != null && response.startsWith("AUTH_FAILED")) {
                JOptionPane.showMessageDialog(frame, response, "Error", JOptionPane.ERROR_MESSAGE);
                frame.dispose();  // Close the client UI
                return;  // End this method
            }
            
            new Thread(() -> {
                while (true) {
                    try {
                        String message = reader.readLine();
                        if (message != null) {
                            messagesArea.append(message + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
