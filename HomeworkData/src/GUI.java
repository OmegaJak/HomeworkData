
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Homework Data");
		
		try {
			FXMLLoader loader = new FXMLLoader(GUI.class.getResource("GUI2.fxml"));
			VBox page = (VBox)loader.load();
			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (IOException e) {
			System.err.println("Error loading GUI2.fxml!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
