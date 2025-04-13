package com.example.lab3;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class Client extends JFrame {
    private JTextField nickField;
    private JTextField answerField;
    private JButton sendButton;
    private JTextArea displayArea;
    private String serverAddress = "localhost";
    private int serverPort = 5000;

    public Client() {
        super("CLIENT");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel(new GridLayout(4, 2, 5, 10));

        JLabel nickLabel = new JLabel("Your Nick:");
        nickField = new JTextField(20);

        JLabel answerLabel = new JLabel("Your Answer:");
        answerField = new JTextField(20);

        sendButton = new JButton("Send Answer>>");
        sendButton.addActionListener(e -> sendAnswer());

        displayArea = new JTextArea();
        displayArea.setEditable(false);

        topPanel.add(nickLabel);
        topPanel.add(nickField);
        topPanel.add(answerLabel);
        topPanel.add(answerField);
        topPanel.add(new JLabel()); // Pusty element dla wyrównania
        topPanel.add(sendButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setVisible(true);
    }

    private void sendAnswer() {
        String nick = nickField.getText().trim();
        String answer = answerField.getText().trim();

        if (nick.isEmpty()) {
            displayMessage("Proszę podać nick!");
            return;
        }

        if (answer.isEmpty()) {
            displayMessage("Proszę podać odpowiedź!");
            return;
        }

        try {
            Socket socket = new Socket(serverAddress, serverPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            //wiadomość w formacie "nick|odpowiedź"
            out.println(nick + "|" + answer);
            displayMessage("Odpowiedź wysłana: " + answer);
            answerField.setText("");
            socket.close();
        } catch (IOException e) {
            displayMessage("Błąd połączenia: " + e.getMessage());
        }
    }

    private void displayMessage(String message) {
        displayArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}