/**
 * Credit for this goes to this blog:
 * http://code.makery.ch/blog/javafx-2-event-handlers-and-change-listeners/
 */

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import com.sun.javafx.css.StyleCache.Key;

public class EventHandlerController {
	
	@FXML
	private TextField textField;
	
	private DataHandler handler;
	
	/**
	 * The constructor. The constructor is called before the initialize()
	 * method.
	 */
	public EventHandlerController() {
		this.handler = new DataHandler();
	}
	
	/**
	 * Initializes the controller class. This method is automatically called
	 * after the fxml file has been loaded.
	 */
	@FXML
	private void initialize() {
		
	}
	
	@FXML
	private void newRow() {
		try {
			this.handler.insertNewRow(-2, 16, handler.csvDir + "\\HomeworkData", "HomeworkDataSem2.csv", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void saveRow() {
		
	}
	
	@FXML
	private void handleKeyReleased() {
		//???????
	}
}