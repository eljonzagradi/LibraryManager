package controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import models.Book;
import models.Database;
import models.General;

public class BooksController implements Initializable {

    @FXML private AnchorPane anpane;
    @FXML private Pane pane;
    @FXML private Button btnRefresh, btnImport, btnDownload;
    @FXML private Button btnRegister, btnEdit, btnSave, btnCancel;
    @FXML private ComboBox < String > cmbAuthor1, cmbAuthor2, cmbLanguage;
    @FXML private Label lblAuthor, lblBarcode, lblBook, lblCategory, lblLanguage;
    @FXML private TableView < Book > tblBooks;

    @FXML private TableColumn < Book, String > tblColAuthor;
    @FXML private TableColumn < Book, String > tblColBarcode;
    @FXML private TableColumn < Book, String > tblColBook;
    @FXML private TableColumn < Book, String > tblColCategory;
    @FXML private TableColumn < Book, Number > tblColCopies;
    @FXML private TableColumn < Book, String > tblColLanguage;
    @FXML private TableColumn < Book, Number > tblColPages;
    @FXML private TableColumn < Book, String > tblColPublisher;

    @FXML private TextField txtBarcode1, txtBarcode2, txtBook, txtBookTitle, txtCategory;
    @FXML private TextField txtKategori, txtLang, txtPages, txtPublisher, txtCopies;

    private ObservableList < Book > books = FXCollections.observableArrayList();
    private ObservableList < String > authors = FXCollections.observableArrayList();
    private ObservableList < String > languages = FXCollections.observableArrayList();

    private String metoda;
    private int bookID;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	
    	if(General.userCategory != 1) {
    		btnDownload.setVisible(false);
    		btnImport.setVisible(false);
    		btnRegister.setVisible(false);
    		btnEdit.setVisible(false);
    	}

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

        UnaryOperator < Change > onlyNumbers = change -> {
            String input = change.getText();
            if (input.matches("[0-9]*")) {
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
        txtBook.setTextFormatter(new TextFormatter < String > (OnlyStringsAndNumbers));
        txtKategori.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtPublisher.setTextFormatter(new TextFormatter < String > (OnlyStringsAndNumbers));
        txtPages.setTextFormatter(new TextFormatter < String > (onlyNumbers));
        txtLang.setTextFormatter(new TextFormatter < String > (onlyStrings));
        txtBarcode2.setTextFormatter(new TextFormatter < String > (barcode));
        txtCopies.setTextFormatter(new TextFormatter < String > (onlyNumbers));

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
        		"SELECT bookID, title, barcode, category, num_pages, language, author, publisher,\n"
        		+ "    copies - COALESCE((SELECT SUM(status = 'BORROWED')\n"
        		+ "    FROM borrowed_books bb WHERE bb.bookID = b.bookID), 0) AS available_copies\n"
        		+ "FROM books b WHERE title LIKE ? AND category LIKE ? AND barcode LIKE ?;";

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
                cmbAuthor2.getItems().addAll(authors);
                cmbAuthor2.setValue(null);
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

    @FXML public void refresh() {

        txtBookTitle.setText(null);
        txtCategory.setText(null);
        cmbLanguage.setValue("All");
        cmbAuthor1.setValue("All");
        txtBarcode1.setText(null);

    }

    @FXML public void registerBook() {

        txtBook.setText(null);
        txtKategori.setText(null);
        cmbAuthor2.setValue(null);
        txtPublisher.setText(null);
        txtPages.setText(null);
        txtLang.setText(null);
        txtBarcode2.setText(null);

        metoda = "SHTO";
        pane.setVisible(true);
        btnRegister.setDisable(true);
        btnEdit.setDisable(true);
        tblBooks.setDisable(true);
        pane.requestFocus();
        General.needsSave = true;

    }

    @FXML public void editBook() {

        if (tblBooks.getSelectionModel().getSelectedItem() != null) {

            Book record = tblBooks.getSelectionModel().getSelectedItem();

            txtBook.setText(record.getTitle());
            txtKategori.setText(record.getCategory());
            cmbAuthor2.setValue(record.getAuthor());
            txtPublisher.setText(record.getPublisher());
            txtPages.setText(record.getPages() + "");
            txtLang.setText(record.getLanguage());
            txtBarcode2.setText(record.getBarcode());

            metoda = "EDIT";
            pane.setVisible(true);
            btnRegister.setDisable(true);
            btnEdit.setDisable(true);
            tblBooks.setDisable(true);
            pane.requestFocus();
            General.needsSave = true;

        } else {
            General.WARNING("Warning", "Please select the record you want to edit !");
        }

    }

    @FXML public void saveBook() {

        if (txtBook.getText() == null ||
            txtBook.getText().trim().isEmpty() ||
            txtKategori.getText() == null ||
            txtKategori.getText().trim().isEmpty() ||
            cmbAuthor2.getValue() == null ||
            cmbAuthor2.getValue().trim().isEmpty() ||
            txtPublisher.getText() == null ||
            txtPublisher.getText().trim().isEmpty() ||
            txtPages.getText() == null ||
            txtPages.getText().trim().isEmpty() ||
            txtLang.getText() == null ||
            txtLang.getText().trim().isEmpty() ||
            txtBarcode2.getText() == null ||
            txtBarcode2.getText().trim().isEmpty() ||
            txtCopies.getText() == null ||
            txtCopies.getText().trim().isEmpty()

        ) {

            General.WARNING("Warning", "Please complete all the required fields to continue");
            return;
        }

        PreparedStatement insert = null, update = null;

        String sqlQueryInsert =
            "INSERT INTO `books` ( `title`, `barcode`, `category`, `num_pages`,\n" +
            " `language`, `author`, `publisher`, `copies`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        String sqlQueryUpdate =
            "UPDATE `books` SET `title` = ?, `barcode` = ?, `category` = ?, `num_pages` = ?,\n" +
            "`language` = ?, `author` = ?, `publisher` = ?, `copies` = ? WHERE `bookID` = ?;";

        try {

            if (metoda.equals("SHTO")) {
                insert = Database.statement(sqlQueryInsert);
                insert.setString(1, txtBook.getText());
                insert.setString(2, txtBarcode2.getText());
                insert.setString(3, txtKategori.getText());
                insert.setString(4, txtPages.getText());
                insert.setString(5, txtLang.getText());
                insert.setString(6, cmbAuthor2.getValue());
                insert.setString(7, txtPublisher.getText());
                insert.setString(8, txtCopies.getText());
                insert.execute();
            } else if (metoda.equals("EDIT") && bookID != 0) {
                update = Database.statement(sqlQueryUpdate);
                update.setString(1, txtBook.getText());
                update.setString(2, txtBarcode2.getText());
                update.setString(3, txtKategori.getText());
                update.setString(4, txtPages.getText());
                update.setString(5, txtLang.getText());
                update.setString(6, cmbAuthor2.getValue());
                update.setString(7, txtPublisher.getText());
                update.setString(8, txtCopies.getText());
                update.executeUpdate();
            } else {
                General.ERROR("ERROR", "SOMETHING WENT WRONG\nPLEASE CONTACT SUPPORT");
                return;
            }

        } catch (SQLException e) {
            General.ERROR("Error", e.getMessage());
            return;
        } finally {

            try {
                if (insert != null)
                    insert.close();

                if (update != null)
                    update.close();

                refresh();
                getLanguages();
                getAuthors();
                getBooks();
                cancelBook();
                anpane.requestFocus();
                General.INFORMATION("Success !", "Changes saved successfully !");

            } catch (SQLException e) {
                General.ERROR("Error", e.getMessage());
            }

        }

    }

    @FXML public void cancelBook() {
        metoda = null;
        pane.setVisible(false);
        btnRegister.setDisable(false);
        btnEdit.setDisable(false);
        tblBooks.setDisable(false);
        General.needsSave = false;
        tblBooks.getSelectionModel().clearSelection();
    }

    @FXML public void downloadTemplate() throws Exception {

        FileChooser fileChooser = new FileChooser();
        InputStream in = null;

        in = getClass().getResourceAsStream("/files/Books.csv");

        fileChooser.setInitialFileName("Books - Template " + LocalDate.now());
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);

        File dest = fileChooser.showSaveDialog(null);
        if (dest != null) {
            if (!dest.exists()) {
                Files.copy(in, dest.toPath());
                General.INFORMATION("Success", "File Downloaded Successfully");

            }
        }
    }

    @FXML public void uploadCVS() {

        if (!General.CONFIRMATION("Information", "Before you procced please make sure that all " +
                "your data is correct and follows the rules below:\n" +
                "1) No empty cells\n" +
                "2) No duplicate barcodes\n" +
                "If these rules are not followed the import process will be " +
                "interrupted or the faulty records will not be recorded\n" +
                "Do you wish to continue ?")) {
            return;
        }

        PreparedStatement insert = null;
        int counter = 1;

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(extFilter);
        String path = "", book = "", category = "", barcode = "", language = "", author = "", publisher = "";
        int copies = 0, pages = 0;
        String sqlQueryInsert =
            "INSERT INTO `books` ( `title`, `barcode`, `category`, `num_pages`,\n" +
            " `language`, `author`, `publisher`, `copies`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        File file = fileChooser.showOpenDialog(null);
        if (file == null) {
            return;
        }
        path = file.getPath();

        try (Reader reader = Files.newBufferedReader(Paths.get(path), Charset.forName("windows-1251")); CSVParser csvParser = new CSVParser(reader,
            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());) {
            for (CSVRecord csvRecord: csvParser) {
                counter++;

                if (csvRecord.get("Book Title").equals("") || csvRecord.get("Book Title") == null) {
                    General.WARNING("Warninig", "Empty Cell A(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Category").equals("") || csvRecord.get("Category") == null) {
                    General.WARNING("Warninig", "Empty Cell B(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Pages").equals("") || csvRecord.get("Pages") == null) {
                    General.WARNING("Warninig", "Empty Cell C(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Author").equals("") || csvRecord.get("Author") == null) {
                    General.WARNING("Warninig", "Empty Cell D(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Publisher").equals("") || csvRecord.get("Publisher") == null) {
                    General.WARNING("Warninig", "Empty Cell E(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Language").equals("") || csvRecord.get("Language") == null) {
                    General.WARNING("Warninig", "Empty Cell F(" + counter + ")");
                    return;
                }

                if (csvRecord.get("BarCode").equals("") || csvRecord.get("BarCode") == null) {
                    General.WARNING("Warninig", "Empty Cell G(" + counter + ")");
                    return;
                }

                if (csvRecord.get("Copies").equals("") || csvRecord.get("Copies") == null) {
                    General.WARNING("Warninig", "Empty Cell H(" + counter + ")");
                    return;
                }

                book = csvRecord.get("Book Title");
                category = csvRecord.get("Category");
                pages = Integer.parseInt(csvRecord.get("Pages"));
                author = csvRecord.get("Author");
                publisher = csvRecord.get("Publisher");
                language = csvRecord.get("Language");
                barcode = csvRecord.get("BarCode");
                copies = Integer.parseInt(csvRecord.get("Copies"));

                try {

                    insert = Database.statement(sqlQueryInsert);
                    insert.setString(1, book);
                    insert.setString(2, barcode);
                    insert.setString(3, category);
                    insert.setInt(4, pages);
                    insert.setString(5, language);
                    insert.setString(6, author);
                    insert.setString(7, publisher);
                    insert.setInt(8, copies);
                    insert.execute();

                } catch (SQLException e) {
                    General.ERROR("Error", e.getMessage() + "\nROW: " + counter);
                    continue;
                }

            }
        } catch (IOException e) {
            General.ERROR("Error", e.getMessage());
        } finally {
            refresh();
            getLanguages();
            getAuthors();
        }

    }
}