package controllers;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.Database;
import models.General;
import models.PageLoader;

public class MainPageController implements Initializable {

    @FXML AnchorPane anpane;
    @FXML BorderPane borderpane;
    @FXML StackPane stackpane;
    @FXML VBox vbox;
    @FXML Button btnLogOut;

    public static Parent root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        borderpane.setPrefWidth(bounds.getWidth());
        borderpane.setPrefHeight(bounds.getHeight());

    }

    @FXML public void logout() throws Exception {

        root = FXMLLoader.load(getClass().getResource("/views/Login.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setScene(scene);
        stage.setTitle("myLibrary.al");
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
        root.requestFocus();

        stage.setOnCloseRequest(new EventHandler < WindowEvent > () {
            public void handle(WindowEvent we) {
                try {
                    System.exit(1);
                    Database.disconnect();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });

        General.activeUser = "";
        LoginController.stage.close();
    }

    @FXML public void openUni() {

        PageLoader loader = new PageLoader();
        Pane view = loader.getPage("University");
        stackpane.getChildren().clear();
        stackpane.getChildren().add(view);
//        borderpane.setCenter(view);
        
    }

    @FXML public void openStudents() {

        PageLoader loader = new PageLoader();
        Pane view = loader.getPage("Students");
        stackpane.getChildren().clear();
        stackpane.getChildren().add(view);
//        borderpane.setCenter(view);
    }

    @FXML public void openBooks() {

        PageLoader loader = new PageLoader();
        Pane view = loader.getPage("Books");
        stackpane.getChildren().clear();
        stackpane.getChildren().add(view);
//        borderpane.setCenter(view);
    }

    @FXML public void openUsers() {

        PageLoader loader = new PageLoader();
        Pane view = loader.getPage("Users");
        stackpane.getChildren().clear();
        stackpane.getChildren().add(view);
//        borderpane.setCenter(view);

    }

}