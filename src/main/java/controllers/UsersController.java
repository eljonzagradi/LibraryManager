package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class UsersController implements Initializable  {

    @FXML
    private AnchorPane anpane;

    @FXML
    private Label lblUsers;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		System.out.println("OK");
	}

}
