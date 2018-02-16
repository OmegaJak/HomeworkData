package com.homeworkdata.ui.core;


import java.io.IOException;

import com.homeworkdata.data.DataHandler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUI extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		if (System.getProperty("os.name").equals("Windows 10")) {
			System.setProperty("glass.accessible.force", "false"); // Fixed freeze on touchscreen devices
		}
		
		primaryStage.setTitle("Homework Data");
		Thread.setDefaultUncaughtExceptionHandler(GUI::showError);
		
		final long startTime = System.currentTimeMillis();
		
		try {
			FXMLLoader loader = new FXMLLoader(GUI.class.getResource("/resources/GUI2.fxml"));
			EventHandlerController controller = new EventHandlerController();
			loader.setController(controller);
			VBox page = (VBox)loader.load();
			Scene scene = new Scene(page);
			primaryStage.setScene(scene);
			primaryStage.show();
			controller.maybeChangesSinceLastSaveCheck = false;
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
	
	private static void showError(Thread t, Throwable e) {
        System.err.println("***Default exception handler***");
        if (Platform.isFxApplicationThread()) {
            showErrorDialog(e);
        } else {
            System.err.println("An unexpected error occurred in "+t);
        }
    }

    private static void showErrorDialog(Throwable e) {
    	DataHandler.errorDialogue(e);
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}
