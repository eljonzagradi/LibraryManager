package controllers;

import java.io.InputStream;
import java.net.URL;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.Database;
import models.General;

public class LoginController implements Initializable {

    @FXML private AnchorPane anpane;
    @FXML private StackPane stackpane;

    @FXML private Button btnLogin;

    @FXML private Label lblLogin, lblUni, lblUsername, lblPassword;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    @FXML private VBox vBoxLogin, vBoxUsers;

    public static Parent root;
    public static Stage stage;
    public static Scene scene;

    Screen screen = Screen.getPrimary();
    double width = screen.getVisualBounds().getWidth();
    double height = screen.getVisualBounds().getHeight();

    private String key = "lRqt1p2IZUqweqwe"; // 128 bit key
    private String initVector = "RandomInitVector"; // 16 bytes IV

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        anpane.setPrefHeight(height);
        anpane.setPrefWidth(width);

        UnaryOperator < Change > OnlyStringsAndNumbers = change -> {
            String input = change.getText();
            if (input.matches("[a-zA-Z]*")) {
                return change;
            }
            return null;
        };

        txtUsername.setTextFormatter(new TextFormatter < String > (OnlyStringsAndNumbers));

        getUsers();
        getUniversity();

    }

    @FXML public void login(ActionEvent event) {

        if (txtUsername.getText() == null ||
            txtUsername.getText().trim().isEmpty() ||
            txtPassword.getText() == null ||
            txtPassword.getText().trim().isEmpty()) {
            General.WARNING("Warning", "Please fill all the fields required to continue");
            return;
        }

        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT * FROM users WHERE username = ? AND password = ?;";

        try {
            select = Database.statement(sqlQuery);
            select.setString(1, txtUsername.getText());
            select.setString(2, General.encrypt(key, initVector, txtPassword.getText()));
            result = select.executeQuery();

            if (result.next()) {
                stage = (Stage) btnLogin.getScene().getWindow();

                General.activeUserID = result.getInt(1);
                General.activeUser = result.getString(2);
                General.userCategory = result.getInt(5);

                if (General.userCategory == 3) {
                    root = FXMLLoader.load(getClass().getResource("/views/StudentView.fxml"));
                } else {
                    root = FXMLLoader.load(getClass().getResource("/views/MainPage.fxml"));
                }

                txtUsername.setText("");
                txtPassword.setText("");

                scene = new Scene(root);

                stage.setScene(scene);

                Screen screen = Screen.getPrimary();
                Rectangle2D bounds = screen.getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                stage.setMaximized(false);
                stage.setResizable(false);

                stage.show();

                stage.setOnCloseRequest(new EventHandler < WindowEvent > () {
                    @Override
                    public void handle(WindowEvent we) {
                        try {
                            System.exit(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                General.INFORMATION("Information", "Wrong username or password");
                txtPassword.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getUniversity() {

        PreparedStatement select = null;
        ResultSet result = null;
        InputStream ins = null;
        Blob blob = null;

        String sqlQuery = "SELECT logo FROM university WHERE universityID = ? LIMIT 1;";

        if (General.hasLogo) {

            ImageView imageview = new ImageView();
            imageview.setPreserveRatio(true);
            imageview.setFitWidth(314);
            imageview.setFitHeight(132);

            try {
                select = Database.statement(sqlQuery);
                select.setInt(1, General.univesityID);
                result = select.executeQuery();

                if (result.next()) {
                    blob = result.getBlob(1);
                    ins = blob.getBinaryStream();
                    imageview.setImage(new Image(ins));
                    stackpane.getChildren().remove(lblLogin);
                    stackpane.getChildren().add(imageview);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        lblUni.setText(General.univesityName);

    }

    public void getUsers() {
        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT username FROM users;";

        try {
            select = Database.statement(sqlQuery);
            result = select.executeQuery();

            while (result.next()) {
                Button user = new Button(result.getString(1));
                user.setPrefWidth(120);
                user.setFont(Font.font(null, FontWeight.BOLD, 15));
                user.setTextAlignment(TextAlignment.CENTER);
                user.setStyle("-fx-background-color: grey;" +
                    "-fx-background-radius:  20 20 20 20;" +
                    "-fx-text-fill: white;");
                user.setOnAction(e -> txtUsername.setText(user.getText()));
                vBoxUsers.getChildren().add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {

            try {
                if (select != null)
                    select.close();
                if (result != null)
                    result.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

    }

}