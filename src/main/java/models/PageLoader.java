package models;

import java.io.IOException;
import java.net.URL;

import controllers.MainPageController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class PageLoader {
	private Pane view;

	public Pane getPage(String fileName) {

		try {
			URL fileUrl = MainPageController.class.getResource("/views/"+fileName+".fxml");

			if(fileUrl == null) {
				throw new java.io.FileNotFoundException("FXML file can not found");
			}

			view = FXMLLoader.load(fileUrl);

		} catch (IOException e) {
			System.out.println("SOMETHING WENT WRONG");
		}

		return view;
	}

}
