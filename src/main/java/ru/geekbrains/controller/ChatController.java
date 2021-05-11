package ru.geekbrains.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.geekbrains.Config;
import ru.geekbrains.MainApp;
import ru.geekbrains.TCPConnection;

import java.io.*;
import java.net.Socket;
import java.util.Objects;


public class ChatController {
    @FXML
    private TextArea msgTA;
    @FXML
    private TextField inputTF;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private FileWriter fileWriter;

    @FXML
    private void sendMessage(ActionEvent event) {
        if (inputTF.getText().isEmpty()) return;
        msgTA.appendText(inputTF.getText() + "\n");
        sendMessage(inputTF.getText());
        writeFile(Config.nickName + ": " + inputTF.getText());
        inputTF.clear();
    }

    private void sendMessage(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка отправки сообщения");
            alert.setHeaderText("Ошибка отправки сообщения");
            alert.setContentText("При отправке сообщения возникла ошибка: " + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void initialize() throws IOException {
        try {
            openLoginWindow();
            MainApp.primaryStage.setTitle(MainApp.primaryStage.getTitle() + " (" + Config.nickName + ")");
            openConnection();
            openFile("history_" + Config.login + ".txt");
            addCloseListener();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка подключения");
            alert.setHeaderText("Сервер не работает");
            alert.setContentText("Не забудь включить сервер!");
            alert.showAndWait();
            e.printStackTrace();
            throw e;
        }
    }

    @FXML
    private void addCloseListener() {
        EventHandler<WindowEvent> onCloseRequest = MainApp.primaryStage.getOnCloseRequest();
        MainApp.primaryStage.setOnCloseRequest(event -> {
            closeConnection();
            closeFile();
            if (onCloseRequest != null) {
                onCloseRequest.handle(event);
            }
        });
    }

    private void openConnection() throws IOException {
        socket = TCPConnection.getSocket();
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    String serverMsg = in.readUTF();
                    if (serverMsg.equalsIgnoreCase("/end")) {
                        msgTA.appendText("Сервер закрыл соединение" + "\n");
                        writeFile(serverMsg + "\n");
                        break;
                    } else {
                        msgTA.appendText(serverMsg + "\n");
                        writeFile(serverMsg + "\n");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void closeConnection() {
        try {
            out.writeUTF("/end");
            socket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openLoginWindow() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/authWindow.fxml")));
        Stage loginStage = new Stage();
        loginStage.setResizable(false);
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(root));
        loginStage.setTitle("Авторизация");
        loginStage.setOnCloseRequest(event -> System.exit(0));
        loginStage.showAndWait();
    }

    private void openFile(String fileName) throws IOException {
        fileWriter = new FileWriter(fileName, true);
    }

    private void writeFile(String text) {
        try {
            fileWriter.write(text + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeFile() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
