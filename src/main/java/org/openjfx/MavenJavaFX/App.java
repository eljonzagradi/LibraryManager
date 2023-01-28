package org.openjfx.MavenJavaFX;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.Database;
import models.General;

/*
 * Entry Point
 */

public class App extends Application {

    public static Parent root;
    public static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {

        if (checkDB() == false) {

            root = FXMLLoader.load(getClass().getResource("/views/RegisterUniversity.fxml"));
            scene = new Scene(root);
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
        } else {

            root = FXMLLoader.load(getClass().getResource("/views/MainPage.fxml"));
            scene = new Scene(root);
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
        }
    }

    public static boolean checkDB() {

        PreparedStatement select = null;
        ResultSet result = null;
        boolean answer = false;

        try {

            select = Database.statement
            		("SELECT universityID, name, IF(logo IS NOT NULL,true,false) AS logo FROM university LIMIT 1;");
            result = select.executeQuery();

            if (result.next()) {
                General.univesityID = result.getInt(1);
                General.univesityName = result.getString(2);
                General.hasLogo = result.getBoolean(3);
                answer = true;
            } else {
                answer = false;
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
        return answer;

    }

    public static void main(String[] args) {
        launch();
    }

}