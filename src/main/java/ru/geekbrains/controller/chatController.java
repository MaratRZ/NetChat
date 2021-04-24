package ru.geekbrains.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class chatController {
    @FXML
    private TextArea msgTA;
    @FXML
    private TextField inputTF;

    @FXML
    private void sendMessage(ActionEvent event) {
        if (inputTF.getText().isEmpty()) return;
        msgTA.appendText(inputTF.getText() + "\n");
        inputTF.clear();
    }
}
