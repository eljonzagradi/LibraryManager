package controllers;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import models.Book;
import models.Database;
import models.General;

public class StudentViewController implements Initializable {

    @FXML private AnchorPane anpane;
    @FXML private Button btnRefresh;
    @FXML private ComboBox < String > cmbAuthor1, cmbLanguage;
    @FXML private Label lblAuthor, lblBarcode, lblBook, lblCategory, lblLanguage, lblUser;
    @FXML private TableView < Book > tblBooks;

    @FXML private TableColumn < Book, String > tblColAuthor;
    @FXML private TableColumn < Book, String > tblColBarcode;
    @FXML private TableColumn < Book, String > tblColBook;
    @FXML private TableColumn < Book, String > tblColCategory;
    @FXML private TableColumn < Book, Number > tblColCopies;
    @FXML private TableColumn < Book, String > tblColLanguage;
    @FXML private TableColumn < Book, Number > tblColPages;
    @FXML private TableColumn < Book, String > tblColPublisher;

    @FXML private TextField txtBarcode1, txtBookTitle, txtCategory;
    private ObservableList < Book > books = FXCollections.observableArrayList();
    private ObservableList < String > authors = FXCollections.observableArrayList();
    private ObservableList < String > languages = FXCollections.observableArrayList();
    
    public static Parent root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	lblUser.setText(General.activeUser);
        tblColAuthor.setCellValueFactory(new PropertyValueFactory < > ("author"));
        tblColBarcode.setCellValueFactory(new PropertyValueFactory < > ("barcode"));
        tblColBook.setCellValueFactory(new PropertyValueFactory < > ("title"));
        tblColCategory.setCellValueFactory(new PropertyValueFactory < > ("category"));
        tblColCopies.setCellValueFactory(new PropertyValueFactory < > ("copies"));
        tblColLanguage.setCellValueFactory(new PropertyValueFactory < > ("language"));
        tblColPages.setCellValueFactory(new PropertyValueFactory < > ("pages"));
        tblColPublisher.setCellValueFactory(new PropertyValueFactory < > ("publisher"));

        UnaryOperator < Change > onlyStrings = change -> {
            String input = change.getText();
            if (input.matches("[a-z A-Z]*")) {
                return change;
            }
            return null;
        };

        UnaryOperator < Change > OnlyStringsAndNumbers = change -> {
            String input = change.getText();
            if (input.matches("[a-zA-Z0-9 ]*")) {
                return change;
            }
            return null;
        };

        UnaryOperator < Change > barcode = change -> {
            String input = change.getText();
            if (input.matches("[a-zA-Z0-9]*")) {
                return change;
            }
            return null;
        };

        txtBookTitle.setTextFormatter(new TextFormatter < String > (OnlyStringsAndNumbers));
        txtCategory.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtBarcode1.setTextFormatter(new TextFormatter < String > (barcode));

        txtBookTitle.textProperty().addListener(e -> getBooks());
        txtCategory.textProperty().addListener(e -> getBooks());
        cmbLanguage.valueProperty().addListener(e -> getBooks());
        cmbAuthor1.valueProperty().addListener(e -> getBooks());
        txtBarcode1.textProperty().addListener(e -> getBooks());

        getLanguages();
        getAuthors();
        getBooks();

    }

    public void getBooks() {

        books.clear();
        PreparedStatement select = null;
        ResultSet result = null;
        String book = null, category = null, barcode = null;

        String sqlQuery =
            "SELECT bookID, title, barcode, category, num_pages,\n" +
            "language, author, publisher, copies FROM books\n" +
            "WHERE title LIKE ? AND category LIKE ? AND barcode LIKE ? ";

        if (txtBookTitle.getText() == null) {
            book = "";
        } else {
            book = txtBookTitle.getText();
        }

        if (txtCategory.getText() == null) {
            category = "";
        } else {
            category = txtCategory.getText();
        }

        if (txtBarcode1.getText() == null) {
            barcode = "";
        } else {
            barcode = txtBarcode1.getText();
        }

        if (cmbLanguage.getValue() != null &&
            !cmbLanguage.getValue().equals("All")) {
            sqlQuery += " AND language = '" + cmbLanguage.getValue() + "' ";
        }

        if (cmbAuthor1.getValue() != null &&
            !cmbAuthor1.getValue().equals("All")) {
            sqlQuery += " AND author = '" + cmbAuthor1.getValue() + "' ";
        }

        try {

            select = Database.statement(sqlQuery);
            select.setString(1, book + "%");
            select.setString(2, category + "%");
            select.setString(3, barcode + "%");
            result = select.executeQuery();

            while (result.next()) {

                books.add(new Book(
                    result.getInt(1),
                    result.getString(2),
                    result.getString(4),
                    result.getInt(5),
                    result.getString(6),
                    result.getString(7),
                    result.getString(8),
                    result.getString(3),
                    result.getInt(9)

                ));
            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                tblBooks.setItems(books);

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getAuthors() {

        authors.clear();
        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT DISTINCT author FROM books;";

        try {

            select = Database.statement(sqlQuery);
            result = select.executeQuery();

            while (result.next()) {

                authors.add(result.getString(1));

            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                cmbAuthor1.getItems().add("All");
                cmbAuthor1.getItems().addAll(authors);
                cmbAuthor1.setValue("All");

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    public void getLanguages() {
        languages.clear();
        PreparedStatement select = null;
        ResultSet result = null;

        String sqlQuery = "SELECT DISTINCT language FROM books;";

        try {

            select = Database.statement(sqlQuery);
            result = select.executeQuery();

            while (result.next()) {

                languages.add(result.getString(1));

            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
        } finally {

            try {

                if (select != null)
                    select.close();

                if (result != null)
                    result.close();

                cmbLanguage.getItems().add("All");
                cmbLanguage.getItems().addAll(languages);
                cmbLanguage.setValue("All");

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

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
            @Override
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

    @FXML public void refresh() {

        txtBookTitle.setText(null);
        txtCategory.setText(null);
        cmbLanguage.setValue("All");
        cmbAuthor1.setValue("All");
        txtBarcode1.setText(null);

    }
}