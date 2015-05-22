
import java.io.IOException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUI extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Homework Data");
		
		final long startTime = System.currentTimeMillis();
		
		try {
			FXMLLoader loader = new FXMLLoader(GUI.class.getResource("GUI2.fxml"));
			VBox page = (VBox)loader.load();
			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					((EventHandlerController)loader.getController()).quit();
					we.consume();
				}
			});
			
			
		} catch (IOException e) {
			System.err.println("Error loading GUI2.fxml!");
			e.printStackTrace();
		}
		
		System.out.println("The loading time was: " + (System.currentTimeMillis() - startTime) + "ms");
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
