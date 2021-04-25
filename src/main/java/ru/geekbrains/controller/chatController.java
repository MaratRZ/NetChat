package ru.geekbrains.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;
import ru.geekbrains.MainApp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class chatController {
    @FXML
    private TextArea msgTA;
    @FXML
    private TextField inputTF;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @FXML
    private void sendMessage(ActionEvent event) {
        if (inputTF.getText().isEmpty()) return;
        msgTA.appendText(inputTF.getText() + "\n");
        sendMessage(inputTF.getText());
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
            openConnection();
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
                        break;
                    } else {
                        msgTA.appendText(serverMsg + "\n");
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
}
