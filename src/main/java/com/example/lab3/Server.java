package com.example.lab3;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server extends JFrame {
    JTextArea displayArea;
    ServerSocket serverSocket;
    Map<String, String> questions = new LinkedHashMap<>();
    int currentQuestionIndex = 0;
    boolean gameOver = false;

    BlockingQueue<String[]> answersQueue = new LinkedBlockingQueue<>();

    public Server() {
        super("Serwer Quizowy");

        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Arial", Font.PLAIN, 14));
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        setVisible(true);

        loadQuestions();//wczytywanie pytan z pliku
        startThreads();// uruchamiam wątki
    }

    private void loadQuestions() {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\W11\\IdeaProjects\\lab3\\src\\main\\java\\com\\example\\lab3\\questions"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    questions.put(parts[0].trim(), parts[1].trim());
                }
            }
            displayArea.append("Wczytano " + questions.size() + " pytań\n");
        } catch (IOException e) {
            displayArea.append("Błąd wczytywania pytań: " + e.getMessage() + "\n");
        }
    }

    private void startThreads() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5000);
                displayArea.append("Serwer nasłuchuje na porcie 5000...\n");

                while (!gameOver) {//producent
                    Socket client = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    String msg = in.readLine(); // np. nick|odpowiedz
                    String ip = client.getInetAddress().getHostAddress();

                    if (msg != null && msg.contains("|")) {
                        String[] data = msg.split("\\|");
                        if (data.length == 2) {
                            answersQueue.put(new String[]{data[0], data[1], ip});
                        }
                    }

                    client.close();
                }
            } catch (Exception e) {
                displayArea.append("Błąd serwera: " + e.getMessage() + "\n");
            }
        }).start();

        new Thread(() -> {//konsument
            java.util.List<String> pytania = new ArrayList<>(questions.keySet());


            if (!pytania.isEmpty()) {
                displayArea.append("\nPYTANIE 1: " + pytania.get(currentQuestionIndex) + "\n");
            }

            while (!gameOver) {
                try {
                    String[] answer = answersQueue.take(); // czekamy aż coś się pojawi

                    String nick = answer[0];
                    String odpowiedz = answer[1];
                    String ip = answer[2];

                    String pytanie = pytania.get(currentQuestionIndex);
                    String poprawna = questions.get(pytanie);

                    if (odpowiedz.equalsIgnoreCase(poprawna)) {
                        displayArea.append(nick + " (" + ip + ") odpowiedział poprawnie!\n");
                        answersQueue.clear(); // usuwamy pozostałe odpowiedzi
                        currentQuestionIndex++;

                        if (currentQuestionIndex < pytania.size()) {
                            displayArea.append("\nPYTANIE " + (currentQuestionIndex + 1) + ": " + pytania.get(currentQuestionIndex) + "\n");
                        } else {
                            displayArea.append("\nKoniec gry! Wszystkie pytania zostały odpowiedziane.\n");
                            gameOver = true;
                            serverSocket.close();
                        }
                    } else {
                        displayArea.append(nick + " (" + ip + ") odpowiedział błędnie.\n");
                    }
                } catch (Exception e) {
                    displayArea.append("Błąd konsumenta: " + e.getMessage() + "\n");
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}
