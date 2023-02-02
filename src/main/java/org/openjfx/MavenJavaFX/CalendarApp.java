package org.openjfx.MavenJavaFX;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarApp extends Application {
    
	private GridPane calendar;
    private Button prevMonth;
    private Button nextMonth;
    private Label monthYear;
    private LocalDate date;
    private Locale locale;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        locale = Locale.getDefault();
        date = LocalDate.now();
        calendar = new GridPane();
        calendar.setAlignment(Pos.CENTER);
        calendar.setHgap(10);
        calendar.setVgap(10);
        calendar.setPadding(new Insets(25, 25, 25, 25));

        prevMonth = new Button("<");
        nextMonth = new Button(">");
        monthYear = new Label();
        updateMonthYearLabel();

        calendar.add(prevMonth, 0, 0);
        calendar.add(monthYear, 1, 0, 2, 1);
        calendar.add(nextMonth, 3, 0);

        prevMonth.setOnAction(event -> prevMonth());
        nextMonth.setOnAction(event -> nextMonth());

        updateCalendar(date.getYear(), date.getMonthValue());

        Scene scene = new Scene(calendar, 400, 400);
        stage.setScene(scene);
        stage.show();
    }

    private void prevMonth() {
        date = date.minusMonths(1);
        updateMonthYearLabel();
        updateCalendar(date.getYear(), date.getMonthValue());
    }

    private void nextMonth() {
        date = date.plusMonths(1);
        updateMonthYearLabel();
        updateCalendar(date.getYear(), date.getMonthValue());
    }

    private void updateMonthYearLabel() {
        monthYear.setText(date.getMonth().getDisplayName(TextStyle.FULL, locale) + " " + date.getYear());
    }

    private void updateCalendar(int year, int month) {
        calendar.getChildren().removeIf(node -> node instanceof Label);
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startingDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int currentRow = 2;
        int currentCol = startingDayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            calendar.add(dayLabel, currentCol, currentRow);
            currentCol++;
            if (currentCol == 7) {
                currentRow++;
                currentCol = 0;
            }
        }
    }
}